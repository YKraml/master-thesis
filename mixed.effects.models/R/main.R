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
      fixed = as.formula(paste0(metric ," ~ 1 + t_pre + t_post + intervention")),
      random = ~ 1 + t_pre + t_post + intervention| id,
      # correlation = corAR1(form = ~ t | id),
      data = data,
      method="ML",
      control = lmeControl(opt = "optim")
    )

    message(paste("   * Drawing plots"))
    save_everything(model, data, folder_name)


    models <- list()
    quantile_data <- split_data_into_quantiles(data, 4, "metric")
    for (data_subset in quantile_data) {
      model <- lme(
        fixed = as.formula(paste0(metric, " ~ t_pre + t_post + intervention")),
        random = ~ 1 + t_pre + t_post + intervention | id,
        data = data_subset,
        method = "ML",
        control = lmeControl(opt = "optim")
      )

      models[[length(models) + 1]] <- model
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


file_name <-"data/bucket_one/ALL.csv"
data <- get_data_from_file(file_name)
metric <- "findingsDensity"

model <- lme(
  fixed = as.formula(paste0(metric, " ~ cleaned_time + t_post + intervention")),
  random = ~ 1 + cleaned_time + t_post + intervention | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

summary(model)

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/shared.R")
source("R/saver.R")
source("R/quantile_splitter.R")
save_everything(model, data, "delete_me")

# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------



