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
library(robustlmm)

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/plotting/plotting_lagged_transition_time.R")
source("R/plotting/plotting_lagged_day_zero.R")
source("R/plotting/plotting_cem_projects.R")
source("R/plotting/plotting_sensitivity.R")

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
  longFiles                = list(name = "longFiles",                display_name = "Long Files",                unit = "Findings"),
  longMethod               = list(name = "longMethod",               display_name = "Long Method",               unit = "Findings"),
  deepNesting              = list(name = "deepNesting",              display_name = "Deep Nesting",              unit = "Findings"),
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

for (metric_data in metrics) {
  metric_name         <- metric_data$name
  message(metric_name)

  for(data_name in c("treatment", "control")){
    message(sprintf(" - %s ", data_name))
    data <- data_all[[data_name]]

    message("   * Creating folder")
    folder_name <- data_name
    out_dir <- file.path("plots", "interrupted_time_series", folder_name, metric_name)
    dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)


    message("   * Removing outliers")
    metric_values <- data[[metric_name]]

    # 2. Calculate Quartiles and IQR
    Q1 <- quantile(metric_values, 0.25, na.rm = TRUE)
    Q3 <- quantile(metric_values, 0.75, na.rm = TRUE)
    IQR_val <- Q3 - Q1

    # 3. Define Upper and Lower Bounds
    lower_bound <- Q1 - 1.5 * IQR_val
    upper_bound <- Q3 + 1.5 * IQR_val

    # 4. Filter the dataframe
    # This keeps only rows within the non-outlier range
    data_clean <- data[metric_values >= lower_bound & metric_values <= upper_bound, ]

    # (Optional) Check how many rows were removed
    rows_removed <- nrow(data) - nrow(data_clean)
    message(paste("   * Removed", rows_removed, "outliers from", metric_name))



    data <- data_clean




    tryCatch({
      message("   * Checking data")
      if (is.null(data)) stop("Data object is NULL")
      if (nrow(data) == 0) stop("Data has 0 rows")

      message("   * Creating model")
      data$time_saturated <- 1 - exp(-1 * data$relative_introduction_time)
      formula <- sprintf("%s ~ time_saturated * (base_slope + intervention_jump + intervention_slope)", metric_name)
      model <- lme(
        fixed = as.formula(formula),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
        data = data,
        method = "ML",
        # correlation = corAR1(form = ~ t | id),
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
# Model comparison
# ------------------------------------------------------------------------------

transition_n_days <- 90
relative_introduction_time <- 4
metric <- "findingsDensity"
metric_unit <- "XXX"
data <- get_data("treatment", transition_n_days)

data$time_saturated <- 1 - exp(-1 * data$relative_introduction_time)
data$log_time <- log(data$relative_introduction_time + 1)


formula <- sprintf("%s ~ time_saturated * (base_slope + intervention_jump + intervention_slope)", metric)
model <- lme(
  fixed = as.formula(formula),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

models <- list()
models[["exponential"]] <- model

# 2. Logarithmic Model
formula_log <- sprintf("%s ~ log_time * (base_slope + intervention_jump + intervention_slope)", metric)
models[["log"]] <- update(model, fixed = as.formula(formula_log))

# 3. Polynomial Models (Degrees 1 to 4)
for (i in 1:4) {
  f_poly <- sprintf("%s ~ poly(relative_introduction_time, %d) * (base_slope + intervention_jump + intervention_slope)", metric, i)
  models[[paste0("poly_", i)]] <- update(model, fixed = as.formula(f_poly))
}

# 4. Natural Spline Models (Degrees of Freedom 1 to 4)
# Note: ns(df=1) is essentially a linear fit
for (i in 1:4) {
  f_spline <- sprintf("%s ~ ns(relative_introduction_time, df = %d) * (base_slope + intervention_jump + intervention_slope)", metric, i)
  models[[paste0("spline_", i)]] <- update(model, fixed = as.formula(f_spline))
}



anova(models[[1]], models[[2]], models[[3]], models[[4]], models[[5]], models[[6]], models[[7]], models[[8]], models[[9]], models[[10]])



# ------------------------------------------------------------------------------
# Testing
# ------------------------------------------------------------------------------

transition_n_days <- 90
metric <- "findingsDensity"
data <- get_data("treatment", transition_n_days)
metric_data <- list(name = "findingsDensity",          display_name = "Findings Density",          unit = "Findings per 1000 LOC")

data$time_saturated <- 1 - exp(-1 * data$relative_introduction_time)


formula <- sprintf("%s ~ time_saturated * (base_slope + intervention_jump + intervention_slope)", metric)
model <- lme(
  fixed = as.formula(formula),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  correlation = corAR1(form = ~ t | id),
  control = lmeControl(opt = "optim")
)

summary(model)


model_2 <- lme(
  fixed = as.formula(formula),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

summary(model_2)

save_everything(model, data, "delet_me", "ALL", metric_data)
