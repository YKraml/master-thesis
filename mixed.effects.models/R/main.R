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
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/plotting/plotting_lagged.R")
source("R/plotting/plotting_cem_projects.R")
source("R/saver.R")
source("R/shared.R")
source("R/lagged.R")


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
  longFilesDensity         = list(name = "longFilesDensity",         display_name = "Long Files Density",        unit = "Findings per 1000 files"),
  longMethodDensity        = list(name = "longMethodDensity",        display_name = "Long Method Density",       unit = "Findings per 1000 methods"),
  deepNestingDensity       = list(name = "deepNestingDensity",       display_name = "Deep Nesting Density",      unit = "Findings per 1000 methods"),
  cloneCoverage            = list(name = "cloneCoverage",            display_name = "Clone Coverage",            unit = "%"),
  averageFileLoc           = list(name = "averageFileLoc",           display_name = "Average File Size",         unit = "LOC")

  # Git metrics
  )


# ------------------------------------------------------------------------------
# Interrupted Time Series Loop
# ------------------------------------------------------------------------------

data_treatment <- get_data("treatment", 90)
data_control   <- get_data("control", 90)
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
      f_full <- sprintf("%s ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)", metric_name)
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
# Data at t = 0
# ------------------------------------------------------------------------------


data_control <- get_data("control", 90)
data_control <- data_control[data_control$t == 0, ]

data_treatment <- get_data("treatment", 90)
data_treatment <- data_treatment[data_treatment$t == 0, ]


plot_at_zero_loc(data_treatment)




plot_at_zero_loc <- function(data){
  hist(log10(data_treatment$linesOfCode + 1),
       xaxt = "n",
       col = "lightblue",
       border = "black",
       xlab = "Lines of Code (Log Scale)",
       ylab = "Number of Projects",
       main = "Project Sizes")
  log_ticks <- 0:10
  axis(1, at = log_ticks, labels = 10^log_ticks)
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
# Lagged
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
# Testing
# ------------------------------------------------------------------------------



















