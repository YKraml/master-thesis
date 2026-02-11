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
source("R/plotting/plotting_effects_distributions.R")

source("R/saver.R")
source("R/shared.R")
source("R/lagged_transition_time.R")
source("R/lagged_day_zero.R")

# ------------------------------------------------------------------------------
# Shared
# ------------------------------------------------------------------------------

languages <- c("java", "python")

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
  longMethods              = list(name = "longMethods",              display_name = "Long Method",               unit = "Findings"),
  deepNesting              = list(name = "deepNesting",              display_name = "Deep Nesting",              unit = "Findings"),
  longFilesDensity         = list(name = "longFilesDensity",         display_name = "Long Files Density",        unit = "Findings per 1000 LOC"),
  longMethodDensity        = list(name = "longMethodDensity",        display_name = "Long Method Density",       unit = "Findings per 1000 LOC"),
  deepNestingDensity       = list(name = "deepNestingDensity",       display_name = "Deep Nesting Density",      unit = "Findings per 1000 LOC"),
  cloneCoverage            = list(name = "cloneCoverage",            display_name = "Clone Coverage",            unit = "%"),
  averageFileLoc           = list(name = "averageFileLoc",           display_name = "Average File Size",         unit = "LOC per File"),

  # Git metrics
  numberOfForks            = list(name = "numberOfForks",           display_name = "Number of Forks",            unit = "Forks"),
  numberOfIssues           = list(name = "numberOfIssues",          display_name = "Number of Issues",           unit = "Issues"),
  numberOfContributors     = list(name = "numberOfContributors",    display_name = "Number of Contributors",     unit = "Contributors")
  )


# ------------------------------------------------------------------------------
# Interrupted Time Series Loop
# ------------------------------------------------------------------------------


for(language in languages) {
  message(paste("---", language, "---"))

  data_treatment <- get_data(language, "treatment", 90)
  data_control   <- get_data(language, "control", 90)

  data_all <- list(
    treatment = data_treatment,
    control   = data_control
  )

  for (metric_data in metrics) {
    metric_name <- metric_data$name
    message(metric_name)

    for(group in c("treatment", "control")){
      message(sprintf(" - %s ", group))
      data <- data_all[[group]]


      message("   * Creating folder")
      out_dir <- file.path("plots", language, "interrupted_time_series", group, metric_name)
      dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)


      message("   * Removing outliers")
      metric_values <- data[[metric_name]]
      Q1 <- quantile(metric_values, 0.25, na.rm = TRUE)
      Q3 <- quantile(metric_values, 0.75, na.rm = TRUE)
      IQR_val <- Q3 - Q1
      lower_bound <- Q1 - 1.5 * IQR_val
      upper_bound <- Q3 + 1.5 * IQR_val
      data_clean <- data[metric_values >= lower_bound & metric_values <= upper_bound, ]


      message(paste("   * Removed", (nrow(data) - nrow(data_clean)), "outliers from", metric_name))
      data <- data_clean


      tryCatch({
        message("   * Creating model")
        formula <- sprintf("%s ~ time_saturated * (1 + base_slope + intervention_jump + intervention_slope)", metric_name)
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
          language = language,
          group = group,
          metric_data = metric_data
        )

      }, error = function(e) {
        warning_msg <- sprintf("FAILURE in Metric: '%s' | Group: '%s'", metric_name, group)
        message(paste0("\n ", warning_msg, "\nError message: ", e$message, "\n"))
      })
    }
  }
}




# ------------------------------------------------------------------------------
# CEM-Projects
# ------------------------------------------------------------------------------

for(language in languages){
  message(paste("---", language, "---"))

  control_data <- read.csv(file.path("data", language, "cem_projects", "control.csv"))
  control_data$time_diff <- (control_data$linterCommitTime - control_data$firstCommitTime) / 60 / 60 / 24 / 365
  control_data$linter_intro_year <- control_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

  treated_data <- read.csv(file.path("data", language, "cem_projects", "treated.csv"))
  treated_data$time_diff <- (treated_data$linterCommitTime - treated_data$firstCommitTime) / 60 / 60 / 24 / 365
  treated_data$linter_intro_year <- treated_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

  out_dir <- file.path("plots", language, "cem_projects")
  dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

  capture.output(wilcox.test(control_data$numberOfStars, treated_data$numberOfStars),                                   file = file.path(out_dir, "wilcox_test_star_counts.txt"))
  capture.output(wilcox.test(control_data$creationYear, treated_data$creationYear),                                     file = file.path(out_dir, "wilcox_test_project_creation_years.txt"))
  capture.output(wilcox.test(control_data$numberOfCommits, treated_data$numberOfCommits),                               file = file.path(out_dir, "wilcox_test_commit_counts.txt"))
  capture.output(wilcox.test(control_data$time_diff, treated_data$time_diff),                                           file = file.path(out_dir, "wilcox_test_linter_ages.txt"))
  capture.output(wilcox.test(control_data$linter_intro_year, treated_data$linter_intro_year),                           file = file.path(out_dir, "wilcox_test_linter_introduction_years.txt"))
  capture.output(wilcox.test(control_data$numberOfCommitsInTwoYearWindow, treated_data$numberOfCommitsInTwoYearWindow), file = file.path(out_dir, "wilcox_test_commit_counts_in_analysis_window.txt"))
  capture.output(wilcox.test(control_data$linterCommitIndex, treated_data$linterCommitIndex),                           file = file.path(out_dir, "wilcox_test_commit_counts_in_analysis_window.txt"))
  capture.output(wilcox.test(control_data$sloc, treated_data$sloc),                                                     file = file.path(out_dir, "wilcox_test_sloc.txt"))
  capture.output(wilcox.test(control_data$numberOfContributors, treated_data$numberOfContributors),                     file = file.path(out_dir, "wilcox_test_number_of_contributors.txt"))


  save_png(expression = plot_star_count(control_data, treated_data),             file = file.path(out_dir, "star_counts.png"))
  save_png(expression = plot_creation_year(control_data, treated_data),          file = file.path(out_dir, "project_creation_years.png"))
  save_png(expression = plot_commit_count(control_data, treated_data),           file = file.path(out_dir, "commit_counts.png"))
  save_png(expression = plot_linter_age(control_data, treated_data),             file = file.path(out_dir, "linter_ages.png"))
  save_png(expression = plot_linter_intro_year(control_data, treated_data),      file = file.path(out_dir, "linter_introduction_years.png"))
  save_png(expression = plot_window_commits(control_data, treated_data),         file = file.path(out_dir, "commit_counts_in_analysis_window.png"))
  save_png(expression = plot_linter_commit_indicies(control_data, treated_data), file = file.path(out_dir, "commit_indices.png"))
  save_png(expression = plot_sloc(control_data, treated_data),                   file = file.path(out_dir, "sloc.png"))
  save_png(expression = plot_number_of_contributors(control_data, treated_data), file = file.path(out_dir, "number_of_contributors.png"))
}




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
# Model comparison - Random Effects
# ------------------------------------------------------------------------------


transition_n_days <- 90
data <- get_data("treatment", transition_n_days)

output_dir <- file.path("plots", "random_effects_comparison")
dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

for (metric in metrics) {
  metric_name <- metric$name
  message(metric_name)

  # Shared parameters
  fixed_formula <- as.formula(sprintf("%s ~ base_slope + intervention_jump + intervention_slope", metric_name))
  ctrl <- lmeControl(opt = "optim")

  # 0-Slope Model: Random Intercept only
  m_int <- lme(fixed = fixed_formula, random = ~ 1 | id, data = data, method = "ML", control = ctrl)

  # 1-Slope Models: Intercept + One Random Effect
  m_base <- lme(fixed = fixed_formula, random = ~ 1 + base_slope | id, data = data, method = "ML", control = ctrl)
  m_jump <- lme(fixed = fixed_formula, random = ~ 1 + intervention_jump | id, data = data, method = "ML", control = ctrl)
  m_slope <- lme(fixed = fixed_formula, random = ~ 1 + intervention_slope | id, data = data, method = "ML", control = ctrl)

  # 2-Slope Models: Intercept + Two Random Effects
  m_base_jump <- lme(fixed = fixed_formula, random = ~ 1 + base_slope + intervention_jump | id, data = data, method = "ML", control = ctrl)
  m_base_slope <- lme(fixed = fixed_formula, random = ~ 1 + base_slope + intervention_slope | id, data = data, method = "ML", control = ctrl)
  m_jump_slope <- lme(fixed = fixed_formula, random = ~ 1 + intervention_jump + intervention_slope | id, data = data, method = "ML", control = ctrl)

  # 3-Slope Model: Full Random Effects Structure
  m_full <- lme(fixed = fixed_formula, random = ~ 1 + base_slope + intervention_jump + intervention_slope | id, data = data, method = "ML", control = ctrl)

  # Compare models
  model_comparison <- anova(m_int, m_base, m_jump, m_slope, m_base_jump, m_base_slope, m_jump_slope, m_full)

  capture.output(
    model_comparison[order(model_comparison$AIC), ],
    file = file.path(output_dir, sprintf("random_effects_comparison.%s.txt", metric_name))
  )
}

# ------------------------------------------------------------------------------
# Model comparison - Interaction Term
# ------------------------------------------------------------------------------


for(language in languages){
  message(paste("---", language, "---"))
  data <- get_data(language, "treatment", 90)

  output_dir <- file.path("plots", language, "time_interaction_comparison")
  dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

  for (metric in metrics) {
    metric_name <- metric$name
    message(metric_name)

    # Shared parameters
    effects <- "(base_slope + intervention_jump + intervention_slope)"
    random_effects <- ~ 1 + base_slope + intervention_jump + intervention_slope | id
    ctrl <- lmeControl(opt = "optim")

    message("1. Linear Model")
    f_lin <- as.formula(sprintf("%s ~ relative_introduction_time * %s", metric_name, effects))
    m_lin <- lme(fixed = f_lin, random = random_effects, data = data, method = "ML", control = ctrl)

    message("2. Saturation Model")
    f_sat <- as.formula(sprintf("%s ~ time_saturated * %s", metric_name, effects))
    m_sat <- lme(fixed = f_sat, random = random_effects, data = data, method = "ML", control = ctrl)

    message("3. Logarithmic Model")
    f_log <- as.formula(sprintf("%s ~ log_time * %s", metric_name, effects))
    m_log <- lme(fixed = f_log, random = random_effects, data = data, method = "ML", control = ctrl)

    message("4. Polynomial Models (Degrees 1 to 3)")
    f_poly1 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 1) * %s", metric_name, effects))
    m_poly1 <- lme(fixed = f_poly1, random = random_effects, data = data, method = "ML", control = ctrl)

    f_poly2 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 2) * %s", metric_name, effects))
    m_poly2 <- lme(fixed = f_poly2, random = random_effects, data = data, method = "ML", control = ctrl)

    f_poly3 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 3) * %s", metric_name, effects))
    m_poly3 <- lme(fixed = f_poly3, random = random_effects, data = data, method = "ML", control = ctrl)


    message("5. Natural Spline Models (Degrees of Freedom 1 to 3)")
    f_spline1 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 1) * %s", metric_name, effects))
    m_spline1 <- lme(fixed = f_spline1, random = random_effects, data = data, method = "ML", control = ctrl)

    f_spline2 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 2) * %s", metric_name, effects))
    m_spline2 <- lme(fixed = f_spline2, random = random_effects, data = data, method = "ML", control = ctrl)

    f_spline3 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 3) * %s", metric_name, effects))
    m_spline3 <- lme(fixed = f_spline3, random = random_effects, data = data, method = "ML", control = ctrl)


    message("6. Compare models")
    model_comparison <- anova(m_lin, m_sat, m_log, m_poly1, m_poly2, m_poly3, m_spline1, m_spline2, m_spline3)

    # Save results sorted by AIC
    capture.output(
      model_comparison[order(model_comparison$AIC), ],
      file = file.path(output_dir, sprintf("%s.%s.time_interaction_comparison.txt", language, metric_name))
    )
  }

}



# ------------------------------------------------------------------------------
# Treated vs Control Effects Distributions
# ------------------------------------------------------------------------------

transition_n_days <- 90

age_ranges <- list(
  c(0, 0.1),
  c(0.1, 0.5),
  c(0.5, 1),
  c(1, 2),
  c(2, 4),
  c(4, 20)
)

data_treatment_full <- get_data("java", "treatment", transition_n_days)
data_treatment_full$time_saturated <- 1 - exp(-1 * data_treatment_full$relative_introduction_time)

data_control_full <- get_data("java", "control", transition_n_days)
data_control_full$time_saturated <- 1 - exp(-1 * data_control_full$relative_introduction_time)



for (range in age_ranges) {
  min_project_age <- range[1]
  max_project_age <- range[2]

  message(sprintf("Time Span: %s to %s years", min_project_age, max_project_age))

  data_treatment <- data_treatment_full[
    data_treatment_full$relative_introduction_time >= min_project_age &
      data_treatment_full$relative_introduction_time <= max_project_age,
  ]

  data_control <- data_control_full[
    data_control_full$relative_introduction_time >= min_project_age &
      data_control_full$relative_introduction_time <= max_project_age,
  ]

  # Optional: Check if we have enough data to proceed, skip if empty to avoid errors
  if (nrow(data_treatment) == 0 || nrow(data_control) == 0) {
    message("    Skipping: Insufficient data for this time span.")
    next
  }


  for (metric in metrics) {
    metric_name <- metric$name
    message(paste("    Metric:", metric_name))


    message("    * Calculating treated model")
    model_treatment <- tryCatch({
      lme(
        fixed = as.formula(sprintf("%s ~ time_saturated * (1 + base_slope + intervention_jump + intervention_slope)", metric_name)),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
        data = data_treatment,
        method = "ML",
        control = lmeControl(opt = "optim")
      )
    }, error = function(e) { message("      Error in treatment model: ", e$message); return(NULL) })

    message("    * Calculating control model")
    model_control <- tryCatch({
      lme(
        fixed = as.formula(sprintf("%s ~ time_saturated * (1 + base_slope + intervention_jump + intervention_slope)", metric_name)),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
        data = data_control,
        method = "ML",
        control = lmeControl(opt = "optim")
      )
    }, error = function(e) { message("      Error in control model: ", e$message); return(NULL) })

    # skip if models did not converge
    if (is.null(model_treatment) || is.null(model_control)) {
      message("    * Skipped")
      next
    }

    df_treatment <- calculate_custom_effects(model_treatment, data_treatment)
    df_control   <- calculate_custom_effects(model_control, data_control)

    output_dir <- file.path("plots", "effects_distributions", metric_name)
    dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

    message("    * Creating plot")
    save_multipanel_png(
      expression = {
        par(oma = c(0, 0, 3, 0))
        plot_treated_vs_control_effects(df_control$intercept,                 df_treatment$intercept,                "Intercept")
        plot_treated_vs_control_effects(df_control$base_slope,                df_treatment$base_slope,               "Slope before Intervention")
        plot_treated_vs_control_effects(df_control$intervention_jump,         df_treatment$intervention_jump,        "Intervention Jump")
        plot_treated_vs_control_effects(df_control$slope_after_intervention,  df_treatment$slope_after_intervention, "Slope after Intervention")
        mtext(
          sprintf("Treatment and Control Effects Distributions - Project Age from %s to %s (Years)", min_project_age, max_project_age),
          outer = TRUE,
          cex = 1.5,
          font = 2
        )
      },
      file = file.path(output_dir, sprintf("%s.%s.%s.predicted.png", metric_name, min_project_age, max_project_age)),
    )


  }
}


# ------------------------------------------------------------------------------
# Testing
# ------------------------------------------------------------------------------




