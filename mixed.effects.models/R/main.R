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

fileNames <- list.files("data", pattern="*.csv", full.names=TRUE, recursive = TRUE)
metrics <- list(
  "findingsDensity",
  "longFilesDensity",
  "longMethodDensity",
  "deepNestingDensity",
  "cloneCoverage",
  "averageFileLoc"
  )

for (file_name in fileNames) {
  print(file_name)
  # Load Data
  data <- get_data_from_file(file_name)

  for (metric in metrics) {
    print(paste(" -",metric))

    # Create folder
    cleanedFileName <- gsub("data.", "", file_name)
    cleanedFileName <- gsub(".csv", "", cleanedFileName)
    folder_name <- paste("plots", "interrupted_time_series",cleanedFileName, metric, sep = "/")
    dir.create(file.path(folder_name), showWarnings = FALSE, recursive = TRUE)

    # Create model
    print(paste("   * Creating model"))
    model <- lme(
      fixed = as.formula(paste0(metric ," ~ 1 + t_pre + t_post + intervention")),
      random = ~ 1 + t_pre + t_post + intervention| id,
      # correlation = corAR1(form = ~ t | id),
      data = data,
      method="ML",
      control = lmeControl(opt = "optim")
    )
    print(paste("   * Finished model"))

    # get the effects for each project
    random_effects <- ranef(model)
    fixed_effects <- fixef(model)
    absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

    # print model results
    capture.output(summary(model), file = file.path(folder_name, "model_summary.txt"))
    capture.output(getVarCov(model), file = file.path(folder_name, "model_var_cov.txt"))

    # comparing models
    #capture.output(anova(model_simple, model), file = file.path(folder_name, "models_anova.txt"))
    #capture.output(aictab(list(model_simple, model), modnames = c("model", "model_simple")), file = file.path(folder_name, "models_aictab.txt"))


    # print tests
    capture.output(t.test(absolute_effects$t_pre, absolute_effects$t_post, paired = TRUE),file = file.path(folder_name, "ttest_tpre_vs_tpost.txt"))
    capture.output(t.test(absolute_effects$intervention),file = file.path(folder_name, "ttest_intervention_vs_0.txt"))
    capture.output(expression = t.test(absolute_effects$`(Intercept)`),file = file.path(folder_name, "ttest_intercept_vs_0.txt"))

    # print metric histogram
    save_png(
      expression = plot_raw_data_hist(data[[metric]], metric),
      file = file.path(folder_name, "hist.png")
    )

    # print fixed effects
    save_png(
      expression = plot_predicted_1(data, model),
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
      expression = plot_residuals_vs_time_1(data, model),
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
        plot_random_effects_histogram(absolute_effects$t_pre, "lightgreen", "t_pre", fixed_effects["t_pre"])
        plot_random_effects_histogram(absolute_effects$t_post, "darkseagreen1", "t_post", fixed_effects["t_post"])
        plot_random_effects_histogram(absolute_effects$intervention, "paleturquoise1", "intervention", fixed_effects[4])
      },
      file = file.path(folder_name, "random_effects_hist.png"),
    )

    # print random effects qq-plot
    save_multipanel_png(
      expression = {
        qqnorm(random_effects[, "(Intercept)"], main = "intercept"); qqline(random_effects[, "(Intercept)"])
        qqnorm(random_effects[, "t_pre"], main = "t_pre"); qqline(random_effects[, "t_pre"])
        qqnorm(random_effects[, "t_post"], main = "t_post"); qqline(random_effects[, "t_post"])
        qqnorm(random_effects[, "intervention"], main = "intervention"); qqline(random_effects[, "intervention"])
      },
      file = file.path(folder_name, "random_effects_qq.png"),
    )

    # print random effects vs residuals
    effects <- c("(Intercept)", "t_pre", "t_post", "intervention")
    plots <- lapply(effects, function(eff) plot_random_effects_vs_residuals(data, model, eff))
    save_png(
      expression = print((plots[[1]] | plots[[2]]) / (plots[[3]] | plots[[4]])),
      file = file.path(folder_name, "random_effects_vs_residuals.png"),
      width = 30,
      height = 20
    )

    # print prediction for project
    project_folder_name <- paste(folder_name,"projects", sep = "/")
    dir.create(file.path(project_folder_name), showWarnings = FALSE, recursive = TRUE)
    for (id in unique(data[["id"]])) {
      save_png(
        expression = plot_project_1(model, id, data),
        file = file.path(project_folder_name, paste0(id, ".png"))
      )
    }


  }
}


# ------------------------------------------------------------------------------

source("R/shared.R")
data <- get_data_from_file("data/bucket_one/ALL.csv")

metric <- "findingsDensity"


model <- lme(
  fixed = as.formula(paste0(metric ," ~ t_pre + t_post + intervention")),
  random = ~ 1 + t_pre + t_post + intervention| id,
  data = data,
  method="ML",
  control = lmeControl(opt = "optim")
)


source("R/plotting/plotting_fixed_effects.R")
plot_predicted_1(data, model)
plot_predicted_2(data, model)


