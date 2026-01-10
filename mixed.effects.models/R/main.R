library(nlme)
library(performance)
library(tidyverse)
library(ggplot2)
library(effects)
library(tidyverse)
library(cowplot)
library(lme4)
library(sjPlot)
library(sjmisc)
library(effects)
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
source("R/saver.R")
source("R/shared.R")
source("R/saver.R")
source("R/lagged.R")

file_names <- list.files("data", pattern="*.csv", full.names=TRUE, recursive = TRUE)
metrics <- list(
  "findingsDensity",
  "comprehensibilityDensity",
  "correctnessDensity",
  "documentationDensity",
  "efficiencyDensity",
  "errorHandlingDensity",
  "redundancyDensity",
  "structureDensity",
  # "securityDensity",
  "longFilesDensity",
  "longMethodDensity",
  "deepNestingDensity",
  "cloneCoverage",
  "averageFileLoc"
  )



for (input_file in file_names) {
  message("Processing: ", input_file)
  # Load Data
  data <- get_data_from_file(input_file)
  linter_name <- gsub("data/|.csv", "", input_file)

  for (metric in metrics) {
    message(paste(" - ",metric))

    out_dir <- file.path("plots", "interrupted_time_series", linter_name, metric)
    dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

    # Create model
    message("   * Creating full model")
    f_full <- sprintf("%s ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)", metric)
    model <- lme(
      fixed = as.formula(f_full),
      random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
      data = data,
      method = "ML",
      control = lmeControl(opt = "optim")
    )

    message("   * Drawing plots")
    save_everything(model, data, out_dir)

    message("   * Calculating anova")
    f_null <- sprintf("%s ~ base_slope * relative_introduction_time", metric)
    model_null <- update(model, fixed = as.formula(f_null))
    capture.output(anova(model, model_null), file = file.path(out_dir, "anova.txt"))


  }
}

# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------



file_name <-"data/bucket_one/ALL.csv"
data <- get_data_from_file(file_name)
data_lagged <- get_lagged_dataset(data, 18)
metric <- "findingsDensity"

model_normal <- lme(
  fixed = as.formula(paste0(metric, " ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)")),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)
folder_name <- "delete_me/normal"
save_everything(model_normal, data, folder_name)

model_lagged <- lme(
  fixed = as.formula(paste0(metric, " ~ relative_introduction_time * (base_slope + intervention_jump + intervention_slope)")),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data_lagged,
  method = "ML",
  control = lmeControl(opt = "optim")
)

folder_name <- "delete_me/lagged"
save_everything(model_lagged, data, folder_name)



# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------

# Lagged
for (input_file in file_names) {
  message("Processing: ", input_file)

  linter_name <- gsub("data/|.csv", "", input_file)

  output_dir <- file.path("plots", "lagged", linter_name)
  dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

  data <- get_data_from_file(input_file)

  for (metric in metrics) {

    output_filename <- sprintf("lagged.%s.%s.png", linter_name, metric)
    output_path <- file.path(output_dir, output_filename)

    if (file.exists(output_path)) {
      message(" - Skip: ", metric)
      next
    }

    message(" - Generating: ", metric)

    result_table <- compare_lagged_models(data, metric = metric, min_lag = -5, max_lag = 25)

    save_png(
      expression = plot_lag_comparison(result_table),
      file = output_path
    )
  }
}

# ------------------------------------------------------------------------------








# ------------------------------------------------------------------------------




