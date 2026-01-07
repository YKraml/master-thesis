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
source("R/shared.R")
source("R/saver.R")
source("R/quantile_splitter.R")

file_names <- list.files("data/bucket_one", pattern="*.csv", full.names=TRUE, recursive = TRUE)
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



for (file_name in file_names) {
  message(file_name)
  # Load Data
  data <- get_data_from_file(file_name)
  linter_name <- gsub("data.", "", file_name)
  linter_name <- gsub(".csv", "", linter_name)

  for (metric in metrics) {
    message(paste(" -",metric))

    folder_name <- file.path("plots", "interrupted_time_series", linter_name, metric)
    dir.create(file.path(folder_name), showWarnings = FALSE, recursive = TRUE)

    # Create model
    message(paste("   * Creating model"))
    model <- lme(
      fixed = as.formula(paste0(metric, " ~ base_slope + intervention_slope + intervention_jump")),
      random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
      data = data,
      method = "ML",
      control = lmeControl(opt = "optim")
    )

    message(paste("   * Drawing plots"))
    save_everything(model, data, folder_name)

    # Anova test, to see if the complete models fits better then a simplified model
    model_null <- lme(
      fixed = as.formula(paste0(metric, " ~ base_slope")),
      random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
      data = data,
      method = "ML",
      control = lmeControl(opt = "optim")
    )
    capture.output(anova(model, model_null), file = file.path(folder_name, "anova.txt"))


    # Split data into quantiles and create a model for each quantile
    models <- list()
    quantile_data <- split_data_into_quantiles(data, 4, metric)
    for (data_subset in quantile_data) {
      models[[length(models) + 1]] <- lme(
        fixed = as.formula(paste0(metric, " ~ base_slope + intervention_slope + intervention_jump")),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
        data = data_subset,
        method = "ML",
        control = lmeControl(opt = "optim")
      )
    }

    save_multipanel_png(
      expression = {
        plot_predicted_1(quantile_data[[1]], models[[1]], "Quantile 1")
        plot_predicted_1(quantile_data[[2]], models[[2]], "Quantile 2")
        plot_predicted_1(quantile_data[[3]], models[[3]], "Quantile 3")
        plot_predicted_1(quantile_data[[4]], models[[4]], "Quantile 4")
      },
      file = file.path(folder_name, "quantiles.png")
    )


  }
}

# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/shared.R")
source("R/saver.R")
source("R/quantile_splitter.R")

file_name <-"data/bucket_one/ALL.csv"
data <- get_data_from_file(file_name)
metric <- "findings"




# calc diff column for findings
data[[metric]] <- as.numeric(data[[metric]])
data <- data[order(data$id, data$time), ]
data$diff <- NA_real_
for (p in unique(data$id)) {
  idx <- which(data$id == p)
  data$diff[idx] <- c(0, diff(data[[metric]][idx]))
}

data$diff <- data$diff / data$linesOfCode * 1000

metric <- "diff"
model <- lme(
  fixed = as.formula(paste0(metric, " ~ base_slope + intervention_slope + intervention_jump")),
  random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

plot_predicted_1(data, model)


# save_everything(model, data, "delete_me")

# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------



# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------



