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
      f_full <- sprintf("%s ~ poly(relative_introduction_time, 3) * (base_slope + intervention_jump + intervention_slope)", metric_name)
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


      message("   * Calculating anova")
      f_null <- sprintf("%s ~ base_slope * relative_introduction_time", metric_name)
      model_null <- update(model, fixed = as.formula(f_null))
      anova_file <- file.path(out_dir, sprintf("%s.%s.anova.txt", folder_name, metric_name))
      capture.output(anova(model, model_null), file = anova_file)

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


data <- get_data("treatment", 90)
metric_name <- "findingsDensity"


f_full <- sprintf("%s ~ (base_slope + intervention_jump + intervention_slope)", metric_name)
model_0 <- lme(
  fixed = as.formula(f_full),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)


f_full <- sprintf("%s ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)", metric_name)
model_1 <- lme(
  fixed = as.formula(f_full),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)


f_full <- sprintf("%s ~ poly(relative_introduction_time, 2) * (base_slope + intervention_jump + intervention_slope)", metric_name)
model_2 <- lme(
  fixed = as.formula(f_full),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

f_full <- sprintf("%s ~ poly(relative_introduction_time, 3) * (base_slope + intervention_jump + intervention_slope)", metric_name)
model_3 <- lme(
  fixed = as.formula(f_full),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)


f_full <- sprintf("%s ~ poly(relative_introduction_time, 4) * (base_slope + intervention_jump + intervention_slope)", metric_name)
model_4 <- lme(
  fixed = as.formula(f_full),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

f_spline <- sprintf("%s ~ ns(relative_introduction_time, df=4) * (base_slope + intervention_jump + intervention_slope)", metric_name)
model_spline <- lme(
  fixed = as.formula(f_spline),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

# --- TESTING ---

anova(model_0, model_1, model_2, model_3, model_4)
AIC(model_0, model_1, model_2, model_3, model_4)


anova(model_0, model_1, model_2, model_3, model_4, model_spline)

summary(model_spline)

model <- model_spline


# 1. Define the specific range you want to plot (0 to 10)
# length.out = 100 ensures a smooth curve with 100 points between 0 and 10
x_range <- seq(from = 0, to = 10, length.out = 100)
my_condition <- list(relative_introduction_time = x_range)

# 2. Generate the plots using this condition

# --- Plot 1: Baseline Level ---
p1 <- plot_predictions(
  model,
  condition = my_condition,
  newdata = datagrid(base_slope = 0, intervention_jump = 0, intervention_slope = 0)
) +
  geom_hline(yintercept = 0, linetype = "dotted") +
  labs(title = "1. Intercept", y = "Predicted Value", x = NULL) +
  theme_minimal()

# --- Plot 2: Base Slope ---
p2 <- plot_slopes(
  model,
  variables = "base_slope",
  condition = my_condition
) +
  geom_hline(yintercept = 0, linetype = "dashed", color = "red") +
  labs(title = "2. Base Slope", y = "Slope Coefficient", x = NULL) +
  theme_minimal()

# --- Plot 3: Intervention Jump ---
p3 <- plot_slopes(
  model,
  variables = "intervention_jump",
  condition = my_condition
) +
  geom_hline(yintercept = 0, linetype = "dashed", color = "red") +
  labs(title = "3. Intervention Jump", y = "Jump Size", x = "Age at Intervention") +
  theme_minimal()

# --- Plot 4: Intervention Slope ---
p4 <- plot_slopes(
  model,
  variables = "intervention_slope",
  condition = my_condition
) +
  geom_hline(yintercept = 0, linetype = "dashed", color = "red") +
  labs(title = "4. Intervention Slope", y = "Slope Change", x = "Age at Intervention") +
  theme_minimal()

# 3. Combine them
combined_plot <- (p1 + p2) / (p3 + p4) +
  plot_annotation(
    title = "ITS Components (Age 0-10 only)",
    subtitle = "Model: Natural Spline (df=4)"
  )

print(combined_plot)



capture.output(summary(model), file = file.path("plots", "summary.txt"))




target_times <- c(0.1, 0.5, 1, 2, 4, 6, 8, 10)

basis <- predict(ns(data$relative_introduction_time, df = 4), target_times)

cf <- fixef(model_spline)

intercept_at_t <- cf["(Intercept)"] + (basis %*% cf[2:5])

# For the others, we use the interaction terms
results <- data.frame(
  time = target_times,
  intercept          = as.vector(intercept_at_t),
  base_slope         = calc_param("base_slope", ":base_slope", basis),
  intervention_jump  = calc_param("intervention_jump", ":intervention_jump", basis),
  intervention_slope = calc_param("intervention_slope", ":intervention_slope", basis)
)

# Revised helper to be more robust with naming
calc_param <- function(main_effect, interaction_suffix, basis_matrix) {
  main_val <- cf[main_effect]
  # Find indices that contain the suffix (e.g., ":base_slope")
  int_idx <- grep(interaction_suffix, names(cf))
  int_coeffs <- cf[int_idx]

  return(as.vector(main_val + (basis_matrix %*% int_coeffs)))
}

print(results)















# 1. Load Data
data <- get_data("treatment", 90)
metric_name <- "findingsDensity"

# 2. Define Manual Break Points
# The breaks correspond to your list: 0, 0.25, 0.5, 1, 2, 4, 6, 8, Inf
manual_breaks <- c(0, 0.25, 0.5, 1, 2, 5, Inf)

# Optional: Create nice labels for the plot/summary
# (R generates standard range labels like "(0.25, 0.5]" automatically, but these are cleaner)
manual_labels <- c("0-0.25", "0.25-0.5", "0.5-1", "1-2", "2-5", ">5")

# 3. Create Groups based on Manual Breaks
data$age_group <- cut(
  data$relative_introduction_time,
  breaks = manual_breaks,
  include.lowest = TRUE, # Ensures 0 is included in the first group
  labels = manual_labels # Remove this line if you prefer default math notation
)

# 4. Define the Simplified Formula
f_split <- sprintf("%s ~ base_slope + intervention_jump + intervention_slope", metric_name)

# 5. Run the Model for Each Split
models_list <- lapply(split(data, data$age_group), function(sub_data) {

  # Check if group has enough data to run a model (e.g., > 10 rows)
  if(nrow(sub_data) < 10) {
    warning(paste("Skipping group", unique(sub_data$age_group), "- not enough data"))
    return(NULL)
  }

  tryCatch({
    lme(
      fixed = as.formula(f_split),
      random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
      data = sub_data,
      method = "ML",
      control = lmeControl(opt = "optim")
    )
  }, error = function(e) {
    message(paste("Model failed for group:", unique(sub_data$age_group), "-", e$message))
    return(NULL)
  })
})

# 6. Check Results (Example: View the Jump coefficient for each group)
results_table <- t(sapply(models_list, function(m) {
  if(!is.null(m)) {
    # Extract Fixed Effects
    fe <- fixef(m)
    # Return specific coefficients of interest
    return(c(
      Jump = fe["intervention_jump"],
      Slope_Change = fe["intervention_slope"]
    ))
  } else {
    return(c(Jump = NA, Slope_Change = NA))
  }
}))

print(results_table)

# 2. Apply to all models and combine
# names(models_list) provides the "Q1...", "Q2..." labels
results_list <- mapply(extract_results, models_list, names(models_list), SIMPLIFY = FALSE)
final_table <- do.call(rbind, results_list)

# 3. Clean up formatting (Optional: rounds numbers for readability)
final_table$Estimate <- round(final_table$Estimate, 4)
final_table$P_Value <- round(final_table$P_Value, 5)

# 4. View the table
print(final_table)

# Optional: If you prefer a "Wide" view to compare side-by-side easily
# (Requires the 'reshape2' or 'tidyr' logic, but here is a base R version)
wide_view <- reshape(
  final_table,
  idvar = "Term",
  timevar = "Group",
  direction = "wide"
)
print(wide_view)
