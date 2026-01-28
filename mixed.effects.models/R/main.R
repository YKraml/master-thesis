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
library(splines)
library(marginaleffects)
library(glmertree)
library(mgcv)

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/plotting/plotting_lagged_transition_time.R")
source("R/plotting/plotting_lagged_day_zero.R")
source("R/plotting/plotting_cem_projects.R")

source("R/saver.R")
source("R/shared.R")
source("R/lagged_transition_time.R")
source("R/lagged_day_zero.R")

# ------------------------------------------------------------------------------
# Shared
# ------------------------------------------------------------------------------

metrics <- list(
  # All findings
  findingsDensity          = list(name = "findingsDensity",          display_name = "Findings Density",          unit = "Findings per 1000 LOC"),

  # Findings groups
  comprehensibilityDensity = list(name = "comprehensibilityDensity", display_name = "Comprehensibility Density", unit = "Findings per 1000 LOC"),
  correctnessDensity       = list(name = "correctnessDensity",       display_name = "Correctness Density",       unit = "Findings per 1000 LOC"),
  documentationDensity     = list(name = "documentationDensity",     display_name = "Documentation Density",     unit = "Findings per 1000 LOC"),
  efficiencyDensity        = list(name = "efficiencyDensity",        display_name = "Efficiency Density",        unit = "Findings per 1000 LOC"),
  errorHandlingDensity     = list(name = "errorHandlingDensity",     display_name = "Error Handling Density",    unit = "Findings per 1000 LOC"),
  redundancyDensity        = list(name = "redundancyDensity",        display_name = "Redundancy Density",        unit = "Findings per 1000 LOC"),
  structureDensity         = list(name = "structureDensity",         display_name = "Structure Density",         unit = "Findings per 1000 LOC"),
  securityDensity          = list(name = "securityDensity",          display_name = "Security Density",          unit = "Findings per 1000 LOC"),

  # Structure metrics
  longFilesDensity         = list(name = "longFilesDensity",         display_name = "Long Files Density",        unit = "Findings per 1000 LOC"),
  longMethodDensity        = list(name = "longMethodDensity",        display_name = "Long Method Density",       unit = "Findings per 1000 LOC"),
  deepNestingDensity       = list(name = "deepNestingDensity",       display_name = "Deep Nesting Density",      unit = "Findings per 1000 LOC"),
  cloneCoverage            = list(name = "cloneCoverage",            display_name = "Clone Coverage",            unit = "%"),
  averageFileLoc           = list(name = "averageFileLoc",           display_name = "Average File Size",         unit = "LOC"),

  # Git metrics
  numberOfForks            = list(name = "numberOfForks",           display_name = "Number of Forks",            unit = "Forks"),
  numberOfIssues           = list(name = "numberOfIssues",          display_name = "Number of Issues",           unit = "Issues"),
  numberOfContributors     = list(name = "numberOfContributors",    display_name = "Number of Contributors",     unit = "Contributors")
  )


# ------------------------------------------------------------------------------
# Interrupted Time Series Loop
# ------------------------------------------------------------------------------

data_treatment <- get_data("treatment", 90)
data_control   <- get_data("control", 90)

# data_treatment <- data_treatment[data_treatment$relative_introduction_time >= 2, ]
# data_control <- data_control[data_control$relative_introduction_time >= 2, ]

data_all <- list(
  treatment = data_treatment,
  control   = data_control
)

for (metric_key in names(metrics)) {
  metric_data         <- metrics[[metric_key]]
  metric_name         <- metric_data$name
  message(metric_name)

  for(data_name in c("treatment", "control")){
    message(sprintf(" - %s ", data_name))
    data <- data_all[[data_name]]

    message("   * Creating folder")
    folder_name <- data_name
    out_dir <- file.path("plots", "interrupted_time_series", folder_name, metric_name)
    dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)


    tryCatch({
      message("   * Checking data")
      if (is.null(data)) stop("Data object is NULL")
      if (nrow(data) == 0) stop("Data has 0 rows")

      message("   * Creating model")
      f_full <- sprintf("%s ~ ns(relative_introduction_time, df=3) * (base_slope + intervention_jump + intervention_slope)", metric_name)
      model <- lme(
        fixed = as.formula(f_full),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
        data = data,
        method = "ML",
        control = lmeControl(opt = "optim")
      )

      message("   * Drawing plots")
      save_everything(
        model = model,
        data = data,
        out_dir = out_dir,
        linter_name = folder_name,
        metric_data = metric_data
      )

      }, error = function(e) {
      warning_msg <- sprintf("FAILURE in Metric: '%s' | Data: '%s'", metric_name, data_name)
      message(paste0("\n ", warning_msg, "\nError message: ", e$message, "\n"))
      })
    }
}


# ------------------------------------------------------------------------------
# CEM-Projects
# ------------------------------------------------------------------------------

control_data <- read.csv("data/cem_projects/control.csv")
control_data$time_diff <- (control_data$linterCommitTime - control_data$firstCommitTime) / 60 / 60 / 24 / 365
control_data$linter_intro_year <- control_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

treated_data <- read.csv("data/cem_projects/treated.csv")
treated_data$time_diff <- (treated_data$linterCommitTime - treated_data$firstCommitTime) / 60 / 60 / 24 / 365
treated_data$linter_intro_year <- treated_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

out_dir <- file.path("plots", "cem_projects")
dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

capture.output(wilcox.test(control_data$numberOfStars, treated_data$numberOfStars), file = file.path("plots/cem_projects", "wilcox_test_star_counts.txt"))
capture.output(wilcox.test(control_data$creationYear, treated_data$creationYear), file = file.path("plots/cem_projects", "wilcox_test_project_creation_years.txt"))
capture.output(wilcox.test(control_data$numberOfCommits, treated_data$numberOfCommits), file = file.path("plots/cem_projects", "wilcox_test_commit_counts.txt"))
capture.output(wilcox.test(control_data$time_diff, treated_data$time_diff), file = file.path("plots/cem_projects", "wilcox_test_linter_ages.txt"))
capture.output(wilcox.test(control_data$linter_intro_year, treated_data$linter_intro_year), file = file.path("plots/cem_projects", "wilcox_test_linter_introduction_years.txt"))
capture.output(wilcox.test(control_data$numberOfCommitsInTwoYearWindow, treated_data$numberOfCommitsInTwoYearWindow), file = file.path("plots/cem_projects", "wilcox_test_commit_counts_in_analysis_window.txt"))
capture.output(wilcox.test(control_data$linterCommitIndex, treated_data$linterCommitIndex), file = file.path("plots/cem_projects", "wilcox_test_commit_counts_in_analysis_window.txt"))
capture.output(wilcox.test(control_data$sloc, treated_data$sloc), file = file.path("plots/cem_projects", "wilcox_test_sloc.txt"))
capture.output(wilcox.test(control_data$numberOfContributors, treated_data$numberOfContributors), file = file.path("plots/cem_projects", "wilcox_test_number_of_contributors.txt"))


source("R/plotting/plotting_cem_projects.R")
save_png(
  expression = plot_star_count(control_data, treated_data),
  file = file.path("plots/cem_projects", "star_counts.png")
)

save_png(
  expression = plot_creation_year(control_data, treated_data),
  file = file.path("plots/cem_projects", "project_creation_years.png")
)

save_png(
  expression = plot_commit_count(control_data, treated_data),
  file = file.path("plots/cem_projects", "commit_counts.png")
)

save_png(
  expression = plot_linter_age(control_data, treated_data),
  file = file.path("plots/cem_projects", "linter_ages.png")
)

save_png(
  expression = plot_linter_intro_year(control_data, treated_data),
  file = file.path("plots/cem_projects", "linter_introduction_years.png")
)

save_png(
  expression = plot_window_commits(control_data, treated_data),
  file = file.path("plots/cem_projects", "commit_counts_in_analysis_window.png")
)

save_png(
  expression = plot_linter_commit_indicies(control_data, treated_data),
  file = file.path("plots/cem_projects", "commit_indices.png")
)

save_png(
  expression = plot_sloc(control_data, treated_data),
  file = file.path("plots/cem_projects", "sloc.png")
)

save_png(
  expression = plot_number_of_contributors(control_data, treated_data),
  file = file.path("plots/cem_projects", "number_of_contributors.png")
)


# ------------------------------------------------------------------------------
# Lagged - Transition Time
# ------------------------------------------------------------------------------

for (metric_key in names(metrics)) {
  metric_data <- metrics[[metric_key]]
  metric_name <- metric_data$name
  message(metric_name)

  # Get data from the different models with the lagged data
  results_list <- get_results(metric_name, 210)

  # Create folder
  out_dir <- file.path("plots", "lagged")
  dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

  # transform data
  results_matrix <- do.call(rbind, results_list)
  x_values <- results_matrix[, 1]
  effect_names <- colnames(results_matrix)[-1]

  # create and safe plot
  save_multipanel_png(
    expression = {
      for (i in 1:length(effect_names)) {
        y_values <- results_matrix[, i + 1]
        effect_name <- effect_names[i]
        plot_lagged(effect_name, x_values, y_values)
      }
    },
    file = file.path(out_dir, sprintf("lagged.%s.png", metric_name)),
    height = 15,
    width = 40,
    mfrow = c(2,4)
  )

}

# ------------------------------------------------------------------------------
# Lagged - Day 0
# ------------------------------------------------------------------------------


output_dir <- file.path("plots", "lagged_day_zero")
dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

data_treatment <- get_data("treatment", 90)
data <- data_treatment




for (metric_name in names(metrics)) {
  metric_unit <- metrics[[metric_name]]

  output_filename <- sprintf("lagged.%s.png", metric_name)
  output_path <- file.path(output_dir, output_filename)

  if (file.exists(output_path)) {
    message(" - Skip: ", metric_name)
    next
  }

  message(" - Generating: ", metric_name)

  # Use metric_name here
  result_table <- compare_lagged_models(data, metric = metric_name, min_lag = -10, max_lag = 20)

  # If plot_lag_comparison accepts a Y-axis label, you can use 'metric_unit' here
  save_png(
    expression = plot_lag_comparison(result_table),
    file = output_path
  )
}



# ------------------------------------------------------------------------------
# Testing
# ------------------------------------------------------------------------------


transition_n_days <- 90
relative_introduction_time <- 4
metric <- "findingsDensity"
metric_unit <- "XXX"
data <- get_data("treatment", transition_n_days)

f_spline <- sprintf("%s ~ ns(relative_introduction_time, df=3) * (base_slope + intervention_jump + intervention_slope)", metric)
model <- lme(
  fixed = as.formula(f_spline),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

summary(model)

source("R/plotting/plotting_fixed_effects.R")
par(mfrow = c(2, 2))
plot_predicted(data, model, 0.1, metric, metric_unit, transition_n_days)
plot_predicted(data, model, 1, metric, metric_unit, transition_n_days)
plot_predicted(data, model, 2, metric, metric_unit, transition_n_days)
plot_predicted(data, model, 4, metric, metric_unit, transition_n_days)
par(mfrow = c(1, 1))


get_parameters(model, 8)




plot_model_sensitivity <- function(model) {
  time_points = 100

  # 1. Define time values
  time_values <- seq(0.01, 10, length.out = time_points)

  # 2. Iterate and collect parameters
  # Assuming get_parameters() is available in your global environment
  param_list <- lapply(time_values, function(t) {
    params <- get_parameters(model, t)
    params$time <- t
    return(params)
  })

  # 3. Combine into a single data frame
  plot_data <- do.call(rbind, param_list)

  # 4. Setup Plotting Grid
  # Determine how many unique parameters we have to create the facets
  unique_params <- unique(plot_data$Parameter)

  # Save original par settings and set new ones

  par(mfrow = c(2, 2))

  # 5. Loop through each parameter and plot
  for (p in unique_params) {
    sub_data <- plot_data[plot_data$Parameter == p, ]

    # Create the base line plot
    plot(sub_data$time, sub_data$Value, type = "l", lwd = 2,
         xlab = "Project Age at Linter Introduction (Years)",
         ylab = "Parameter Estimate",
         main = p, # Facet title
         col = "red")

    grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")
  }



  par(mfrow = c(1,1))
}

plot_model_sensitivity(model)


