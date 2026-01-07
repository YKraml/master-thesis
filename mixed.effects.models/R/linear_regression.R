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

for (file_name in file_names) {
  message(file_name)
  # Load Data
  data <- get_data_from_file_for_linear_regression(file_name)

  linter_name <- gsub("data.", "", file_name)
  linter_name <- gsub(".csv", "", linter_name)

  for (metric in metrics) {
    message(paste(" -",metric))

    folder_name <- file.path("plots", "linear_regression", linter_name, metric)
    dir.create(file.path(folder_name), showWarnings = FALSE, recursive = TRUE)


    # Create model
    message(paste("   * Creating model"))
    model <- lme(
      fixed = as.formula(paste0(metric, " ~ slope")),
      random = ~ 1 + slope | id,
      data = data,
      method = "ML",
      control = lmeControl(opt = "optim")
    )

    message(paste("   * Plotting"))
    save_everything_linear_regression(model, data, folder_name)

  }
}



# ------------------------------------------------------------------------------

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/shared.R")
source("R/saver.R")
source("R/quantile_splitter.R")

# Load Data
data <- get_data_from_file_for_linear_regression("data/bucket_one/ALL.csv")
metric <- "findingsDensity"

model <- lme(
  fixed = as.formula(paste0(metric, " ~ slope")),
  random = ~ 1 + slope | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

folder_name <- "delete_me"
save_everything_linear_regression(model, data, folder_name)

# ------------------------------------------------------------------------------




save_everything_linear_regression <- function(model, data, folder_name){
  dir.create(file.path(folder_name), showWarnings = FALSE, recursive = TRUE)

  # get the effects for each project
  random_effects <- ranef(model)
  fixed_effects <- fixef(model)
  absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

  # print model results
  capture.output(summary(model), file = file.path(folder_name, "model_summary.txt"))

  # print metric histogram
  save_png(
    expression = plot_raw_data_hist(data[[metric]], metric),
    file = file.path(folder_name, "hist.png")
  )

  # print fixed effects
  save_png(
    expression = plot_predicted_2(data, model),
    file = file.path(folder_name, "predicted.png")
  )

  # print qq of residuals
  save_png(
    expression = plot_residuals_qq(data, model),
    file = file.path(folder_name, "residuals_qq.png")
  )

  # print residuals vs fitted
  save_png(
    expression = plot_residuals_vs_fitted(data, model),
    file = file.path(folder_name, "residuals_vs_fitted.png")
  )

  # print residuals vs time
  save_png(
    expression = plot_residuals_vs_time_2(data, model),
    file = file.path(folder_name, "residuals_vs_time.png")
  )

  # print residuals histogram
  save_png(
    expression = plot_residuals_histogram(data, model),
    file = file.path(folder_name, "residuals_hist.png")
  )

  # print Auto- and Cross- Covariance and -Correlation Function Estimation
  save_png(
    expression = acf(resid(model, type = "normalized")),
    file = file.path(folder_name, "acf.png")
  )

  # print random effects correlation
  save_png(
    expression = plot_random_effects_correlation(model, metric),
    file = file.path(folder_name, "random_effects_correlation.png")
  )

  # print random effects histograms
  save_multipanel_png(
    expression = {
      plot_random_effects_histogram(absolute_effects$`(Intercept)`, "lightblue", "intercept", fixed_effects["(Intercept)"])
      plot_random_effects_histogram(absolute_effects$slope, "darkseagreen1", "slope", fixed_effects["slope"])
    },
    file = file.path(folder_name, "random_effects_hist.png"),
    mfrow = c(1, 2),
    width = 30,
    height = 10
  )

  # print random effects qq-plot
  save_multipanel_png(
    expression = {
      qqnorm(random_effects[, "(Intercept)"], main = "intercept"); qqline(random_effects[, "(Intercept)"])
      qqnorm(random_effects[, "slope"], main = "slope"); qqline(random_effects[, "slope"])
    },
    file = file.path(folder_name, "random_effects_qq.png"),
    mfrow = c(1, 2),
    width = 30,
    height = 10
  )

  # print prediction for project
  effects <- c("(Intercept)", "slope")
  plots <- lapply(effects, function(eff) plot_random_effects_vs_residuals(data, model, eff))
  save_png(
    expression = print((plots[[1]] / plots[[2]])),
    file = file.path(folder_name, "random_effects_vs_residuals.png"),
    width = 15,
    height = 20
  )

  # print prediction for project
  project_folder_name <- paste(folder_name,"projects", sep = "/")
  dir.create(file.path(project_folder_name), showWarnings = FALSE, recursive = TRUE)
  # for (id in unique(data[["id"]])) {
  #   save_png(
  #     expression = plot_project_2(model, id, data),
  #     file = file.path(project_folder_name, paste0(id, ".png"))
  #   )
  # }
}


