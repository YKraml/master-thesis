library(nlme)
library(performance)
library(tidyverse)
library(ggplot2)
library(effects)
library(cowplot)
library(lme4)
library(sjPlot)
library(sjmisc)
library(sjstats)
library(lattice)
library(lmerTest)
library(DHARMa)
library(dplyr)
library(glmmTMB)
library(AICcmodavg)
library(tibble)
library(patchwork)
library(RColorBrewer)
library(ggeffects)
library(multcomp)

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_projects.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/plotting/plotting_lagged.R")
source("R/saver.R")
source("R/shared.R")
source("R/lagged.R")

data_control <- get_data("control", 90)
data_treatment <- get_data("treatment", 90)

folder_name <- "treatment"
data <- data_treatment

folder_name <- "control"
data <- data_control



metrics <- list(
  findingsDensity          = list(name = "findingsDensity",          display_name = "Findings Density",          unit = "Findings per 1000 LOC"),
  comprehensibilityDensity = list(name = "comprehensibilityDensity", display_name = "Comprehensibility Density", unit = "Findings per 1000 LOC"),
  correctnessDensity       = list(name = "correctnessDensity",       display_name = "Correctness Density",       unit = "Findings per 1000 LOC"),
  documentationDensity     = list(name = "documentationDensity",     display_name = "Documentation Density",     unit = "Findings per 1000 LOC"),
  efficiencyDensity        = list(name = "efficiencyDensity",        display_name = "Efficiency Density",        unit = "Findings per 1000 LOC"),
  errorHandlingDensity     = list(name = "errorHandlingDensity",     display_name = "Error Handling Density",    unit = "Findings per 1000 LOC"),
  redundancyDensity        = list(name = "redundancyDensity",        display_name = "Redundancy Density",        unit = "Findings per 1000 LOC"),
  structureDensity         = list(name = "structureDensity",         display_name = "Structure Density",         unit = "Findings per 1000 LOC"),
  # securityDensity        = list(name = "securityDensity",          display_name = "Security Density",          unit = "Findings per 1000 LOC"),
  longFilesDensity         = list(name = "longFilesDensity",         display_name = "Long Files Density",        unit = "Findings per 1000 files"),
  longMethodDensity        = list(name = "longMethodDensity",        display_name = "Long Method Density",       unit = "Findings per 1000 methods"),
  deepNestingDensity       = list(name = "deepNestingDensity",       display_name = "Deep Nesting Density",      unit = "Findings per 1000 methods"),
  cloneCoverage            = list(name = "cloneCoverage",            display_name = "Clone Coverage",            unit = "%"),
  averageFileLoc           = list(name = "averageFileLoc",           display_name = "Average File Size",         unit = "LOC")
)



# ------------------------------------------------------------------------------
# Interrupted Time Series Loop
# ------------------------------------------------------------------------------
for (metric_key in names(metrics)) {
  metric_data         <- metrics[[metric_key]]
  metric_name         <- metric_data$name

  message(paste0(" - ", metric_name))

  out_dir <- file.path("plots", "interrupted_time_series", folder_name, metric_name)
  dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

  # Create model
  message("   * Creating model")
  f_full <- sprintf("%s ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)", metric_name)
  model <- lme(
    fixed = as.formula(f_full),
    random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
    data = data,
    # correlation = corAR1(form = ~ t | id),
    method = "ML",
    control = lmeControl(opt = "optim")
  )

  head(data)

  message("   * Drawing plots")
  save_everything(
    model = model,
    data = data,
    out_dir = out_dir,
    linter_name = folder_name,
    metric_data = metric_data
    )

  message("   * Calculating anova")
  f_null <- sprintf("%s ~ base_slope * relative_introduction_time", metric_name)
  model_null <- update(model, fixed = as.formula(f_null))
  capture.output(anova(model, model_null), file = file.path(out_dir, sprintf("%s.%s.anova.txt", folder_name, metric_name)))
}







# ------------------------------------------------------------------------------
# Lagged Loop
# ------------------------------------------------------------------------------

for (input_file in file_names) {
  message("Processing: ", input_file)

  linter_name <- gsub("data/time_series/|.csv", "", input_file)

  output_dir <- file.path("plots", "lagged", linter_name)
  dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

  data <- get_data_from_file(input_file)

  # UPDATED LOOP: Iterate over names(metrics)
  for (metric_name in names(metrics)) {
    metric_unit <- metrics[[metric_name]]

    output_filename <- sprintf("lagged.%s.%s.png", linter_name, metric_name)
    output_path <- file.path(output_dir, output_filename)

    if (file.exists(output_path)) {
      message(" - Skip: ", metric_name)
      next
    }

    message(" - Generating: ", metric_name)

    # Use metric_name here
    result_table <- compare_lagged_models(data, metric = metric_name, min_lag = -10, max_lag = 30)

    # If plot_lag_comparison accepts a Y-axis label, you can use 'metric_unit' here
    save_png(
      expression = plot_lag_comparison(result_table),
      file = output_path
    )
  }
}





# ------------------------------------------------------------------------------
# Projects
# ------------------------------------------------------------------------------

data <- read.csv("data/project_data/projects.csv")

output_dir <- file.path("plots", "projects")
dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

save_png(
  expression = plot_number_of_stars_hist(data),
  file = file.path(output_dir, "number_of_stars.png")
)

save_png(
  expression = plot_age_at_intro_hist(data),
  file = file.path(output_dir, "project_age_at_intro.png")
)

save_png(
  expression = plot_project_creation_year_hist(data),
  file = file.path(output_dir, "project_creation_dates.png")
)


# ------------------------------------------------------------------------------
# CEM-Projects
# ------------------------------------------------------------------------------

control_data <- read.csv("data/cem_projects/control.csv")
control_data$time_diff <- (control_data$linterCommitTime - control_data$firstCommitTime) / 60 / 60 / 24 / 365
control_data$linter_intro_year <- control_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

treated_data <- read.csv("data/cem_projects/treated.csv")
treated_data$time_diff <- (treated_data$linterCommitTime - treated_data$firstCommitTime) / 60 / 60 / 24 / 365
treated_data$linter_intro_year <- treated_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

# Number of stars
df_control <- data.frame(stars = control_data$numberOfStars, group = "Control")
df_treated <- data.frame(stars = treated_data$numberOfStars, group = "Treated")
combined_data <- rbind(df_control, df_treated)
ggplot(combined_data, aes(x = stars + 1, fill = group)) +
  geom_density(alpha = 0.5) +
  scale_x_log10() +
  labs(title = "Star Count of Projects",
       x = "Number of Stars",
       y = "Density",
       fill = "Group") +
  theme_minimal()


# Creation year
df_control <- data.frame(year = control_data$creationYear, group = "Control")
df_treated <- data.frame(year = treated_data$creationYear, group = "Treated")
combined_data <- rbind(df_control, df_treated)
ggplot(combined_data, aes(x = year, fill = group)) +
  geom_density(alpha = 0.5) +
  labs(title = "Creation Years of Projects",
       x = "Year",
       y = "Density") +
  theme_minimal()



# Number of commits
df_control <- data.frame(commits = control_data$numberOfCommits, group = "Control")
df_treated <- data.frame(commits = treated_data$numberOfCommits, group = "Treated")
combined_data <- rbind(df_control, df_treated)
ggplot(combined_data, aes(x = commits + 1, fill = group)) +
  geom_density(alpha = 0.5) +
  scale_x_log10() +
  labs(title = "Number of Commits of Projects",
       x = "Number of Commits",
       y = "Density",
       fill = "Group") +
  theme_minimal()


# Time between first commit and linter commit
df_control <- data.frame(time_diff = control_data$time_diff, group = "Control")
df_treated <- data.frame(time_diff = treated_data$time_diff, group = "Treated")
combined_data <- rbind(df_control, df_treated)
ggplot(combined_data, aes(x = time_diff, fill = group)) +
  geom_density(alpha = 0.5) +
  labs(title = "Age at Linter Intro",
       x = "Age at Linter Intro (Years)",
       y = "Density") +
  theme_minimal()

# Year of linter intro (absolut)
df_control <- data.frame(year = control_data$linter_intro_year, group = "Control")
df_treated <- data.frame(year = treated_data$linter_intro_year, group = "Treated")
combined_data <- rbind(df_control, df_treated)
ggplot(combined_data, aes(x = year, fill = group)) +
  geom_density(alpha = 0.5) +
  labs(title = "Introduction Year of Linter",
       x = "Year",
       y = "Density") +
  theme_minimal()



# Number of Commits in Analysis Window
df_control <- data.frame(commits = control_data$numberOfCommitsInTwoYearWindow, group = "Control")
df_treated <- data.frame(commits = treated_data$numberOfCommitsInTwoYearWindow, group = "Treated")
combined_data <- rbind(df_control, df_treated)
ggplot(combined_data, aes(x = commits, fill = group)) +
  geom_density(alpha = 0.5) +
  scale_x_log10() +
  labs(title = "Distribution Comparison: Control vs Treated",
       x = "Number of Commits in Analysis Window (Log Scale)",
       y = "Density") +
  theme_minimal()



# ------------------------------------------------------------------------------
# Testing
# ------------------------------------------------------------------------------

metric_name <- "findingsDensity"
metric_display_name <- "Findings Density"
metric_unit <- "Findings per 1000 LOC"
f_full <- sprintf("%s ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)", metric_name)
model <- lme(
  fixed = as.formula(f_full),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

transition_n_days <- 90

source("R/plotting/plotting_fixed_effects.R")
save_multipanel_png(
  expression = {
    plot_predicted(data, model,  0.1, metric_display_name, metric_unit, transition_n_days)
    plot_predicted(data, model,  0.5, metric_display_name, metric_unit, transition_n_days)
    plot_predicted(data, model,  1  , metric_display_name, metric_unit, transition_n_days)
    plot_predicted(data, model,  2  , metric_display_name, metric_unit, transition_n_days)
    plot_predicted(data, model,  4  , metric_display_name, metric_unit, transition_n_days)
    plot_predicted(data, model,  6  , metric_display_name, metric_unit, transition_n_days)
    plot_predicted(data, model,  8  , metric_display_name, metric_unit, transition_n_days)
    plot_predicted(data, model, 10  , metric_display_name, metric_unit, transition_n_days)
  },
  file = file.path("delete_me", "predicted.png"),
  height = 15,
  width = 40,
  mfrow = c(2,4)
)



