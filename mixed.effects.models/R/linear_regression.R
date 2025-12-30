library(nlme)
library(performance)
library(tidyverse)
library(ggplot2)
library(ggeffects)
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

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/shared.R")

file_names <- list.files("data", pattern="*.csv", full.names=TRUE, recursive = TRUE)
metrics <- list(
  "clonesDensity",
  "longFilesDensity",
  "longMethodDensity",
  "deepNestingDensity",
  "findingsDensity",
  "cloneCoverage",
  "averageFileLoc"
)

for (file_name in file_names) {
  print(file_name)
  # Load Data
  data <- get_data_from_file(file_name)
  data <- data %>%
    group_by(id, t_post) %>%
    filter(n() == 1) %>%
    ungroup()

  for (metric in metrics) {
    print(paste(" -",metric))

    # Create folder
    cleaned_file_name <- gsub("data.", "", file_name)
    cleaned_file_name <- gsub(".csv", "", cleaned_file_name)
    folder_name <- paste("plots", "linear_regression", cleaned_file_name, metric, sep = "/")
    dir.create(file.path(folder_name), showWarnings = FALSE, recursive = TRUE)


    model <- lme(
      fixed = as.formula(paste0(metric ," ~ 1 + t_post")),
      random = ~ 1 + t_post | id,
      data = data,
      method="ML",
      control = lmeControl(opt = "optim")
    )

    random_effects <- ranef(model)
    fixed_effects <- fixef(model)
    absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

    # print model results
    capture.output(summary(model), file = file.path(folder_name, "model_summary.txt"))
    capture.output(getVarCov(model), file = file.path(folder_name, "model_var_cov.txt"))

    # print tests
    capture.output(t.test(absolute_effects$t_post), file = file.path(folder_name, "ttest_t_post_vs_0.txt"))
    capture.output(t.test(absolute_effects$`(Intercept)`), file = file.path(folder_name, "ttest_intercept_vs_0.txt"))

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
      expression = plot_random_effects_correlation(model),
      file = file.path(folder_name, "random_effects_correlation.png")
    )

    # print random effects histograms
    save_multipanel_png(
      expression = {
        plot_random_effects_histogram(absolute_effects$`(Intercept)`, "lightblue", "intercept", fixed_effects["(Intercept)"])
        plot_random_effects_histogram(absolute_effects$t_post, "darkseagreen1", "t_post", fixed_effects["t_post"])
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
        qqnorm(random_effects[, "t_post"], main = "t_post"); qqline(random_effects[, "t_post"])
      },
      file = file.path(folder_name, "random_effects_qq.png"),
      mfrow = c(1, 2),
      width = 30,
      height = 10
    )

    # print prediction for project
    effects <- c("(Intercept)", "t_post")
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
    for (id in unique(data[["id"]])) {
      save_png(
        expression = plot_project_2(model, id, data),
        file = file.path(project_folder_name, paste0(id, ".png"))
      )
    }

  }
}



# ------------------------------------------------------------------------------

# Load Data
data <- get_data_from_file("data/bucket_one/CHECKSTYLE.csv")

metric <- "findingsDensity"

model <- lme(
  fixed = as.formula(paste0(metric ," ~ t_post")),
  random = ~ 1 + t_post | id,
  data = data,
  method="ML",
  control = lmeControl(opt = "optim")
)


summary(model)

# ------------------------------------------------------------------------------



