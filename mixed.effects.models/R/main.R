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

capture.output(wilcox.test(control_data$numberOfStars, treated_data$numberOfStars), file = file.path("plots/cem_projects", "wilcox_test_star_counts.png.txt"))
capture.output(wilcox.test(control_data$creationYear, treated_data$creationYear), file = file.path("plots/cem_projects", "wilcox_test_project_creation_years.png.txt"))
capture.output(wilcox.test(control_data$numberOfCommits, treated_data$numberOfCommits), file = file.path("plots/cem_projects", "wilcox_test_commit_counts.png.txt"))
capture.output(wilcox.test(control_data$time_diff, treated_data$time_diff), file = file.path("plots/cem_projects", "wilcox_test_linter_ages.png.txt"))
capture.output(wilcox.test(control_data$linter_intro_year, treated_data$linter_intro_year), file = file.path("plots/cem_projects", "wilcox_test_linter_introduction_years.png.txt"))
capture.output(wilcox.test(control_data$numberOfCommitsInTwoYearWindow, treated_data$numberOfCommitsInTwoYearWindow), file = file.path("plots/cem_projects", "wilcox_test_commit_counts_in_analysis_window.png.txt"))
capture.output(wilcox.test(control_data$linterCommitIndex, treated_data$linterCommitIndex), file = file.path("plots/cem_projects", "wilcox_test_commit_counts_in_analysis_window.png.txt"))


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

# ------------------------------------------------------------------------------
# Testing
# ------------------------------------------------------------------------------




