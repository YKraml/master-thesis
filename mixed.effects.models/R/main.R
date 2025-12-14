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

source("R/plotting.R")

fileNames <- list.files("data", pattern="*.csv", full.names=TRUE, recursive = TRUE)
metrics <- list(
  # "files",
  # "linesOfCode",
  # "sourceLinesOfCode",
  # "clones",
  # "longFiles",
  # "longMethods",
  # "deepNesting",
  # "findings",
  # "clonesDensity",
  "longFilesDensity",
  "longMethodDensity",
  "deepNestingDensity",
  "findingsDensity",
  # "cloneCoverage",
  "averageFileLoc"
  # "averageFileSloc"
  )

for (fileName in fileNames) {
  print(fileName)
  # Load Data
  dataNotClean <- read.csv(fileName)
  data <- dataNotClean[complete.cases(dataNotClean), ]
  data$cleaned_time <- ifelse(
    data$intervention == 0,
    data$t_pre,
    data$t_post
  )

  for (metric in metrics) {
    print(paste(" -",metric))

    # Create folder
    cleanedFileName <- gsub("data.", "", fileName)
    cleanedFileName <- gsub(".csv", "", cleanedFileName)
    folderName <- paste("plots",cleanedFileName, metric, sep = "/")
    dir.create(file.path(folderName), showWarnings = FALSE, recursive = TRUE)


    # Create model
    model <- lme(
      fixed = as.formula(paste0(metric ," ~ t_pre + t_post + intervention")),
      random = ~ 1 + t_pre + t_post + intervention| id,
      correlation = corAR1(form = ~ cleaned_time | id),
      data = data,
      method="ML",
      control = lmeControl(opt = "optim")
    )

    # Create simple model without ar1
    model_simple <- lme(
      fixed = as.formula(paste0(metric ," ~ t_pre + t_post + intervention")),
      random = ~ 1 + t_pre + t_post + intervention| id,
      data = data,
      method="ML",
      control = lmeControl(opt = "optim")
    )

    random_effects <- ranef(model)
    fixed_effects <- fixef(model)
    absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

    # print model results
    capture.output(summary(model), file = file.path(folderName, "model_summary.txt"))
    capture.output(summary(model_simple), file = file.path(folderName, "model_simple_summary.txt"))
    capture.output(anova(model_simple, model), file = file.path(folderName, "models_anova.txt"))
    capture.output(aictab(list(model_simple, model), modnames = c("model", "model_simple")), file = file.path(folderName, "models_aictab.txt"))


    # print tests
    capture.output(
      t.test(absolute_effects$t_pre, absolute_effects$t_post, paired = TRUE),
      file = file.path(folderName, "ttest_tpre_vs_tpost.txt")
      )

    capture.output(
      t.test(absolute_effects$intervention),
      file = file.path(folderName, "ttest_intervention_vs_0.txt")
    )

    capture.output(
      t.test(absolute_effects$`(Intercept)`),
      file = file.path(folderName, "ttest_intercept_vs_0.txt")
    )



    # print metric histogram
    plotPath <- paste(folderName, "hist.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_raw_data_hist(data[[metric]], metric)
    dev.off()

    # print fixed effects
    plotPath <- paste(folderName, "predicted.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_predicted(data, model)
    dev.off()

    # print qq of residuals
    plotPath <- paste(folderName, "qq.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_qq(data, model)
    dev.off()

    # print residuals vs fitted
    plotPath <- paste(folderName, "residuals_vs_fitted.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_residuals_vs_fitted(data, model)
    dev.off()

    # print residuals histogram
    plotPath <- paste(folderName, "residuals_hist.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_residuals_histogram(data, model)
    dev.off()

    plotPath <- paste(folderName, "random_effects_hist_t_pre.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_random_effects_histogram(absolute_effects$t_pre, "lightgreen", "t_pre")
    dev.off()

    plotPath <- paste(folderName, "random_effects_hist_t_post.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_random_effects_histogram(absolute_effects$t_post, "darkseagreen1", "t_post")
    dev.off()

    plotPath <- paste(folderName, "random_effects_hist_intervention.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_random_effects_histogram(absolute_effects$intervention, "paleturquoise1", "intervention")
    dev.off()

    plotPath <- paste(folderName, "random_effects_hist_intercept.png", sep = "/")
    png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
    plot_random_effects_histogram(absolute_effects$`(Intercept)`, "lightblue", "intercept")
    dev.off()

    # create folder
    projectFolderName <- paste(folderName,"projects", sep = "/")
    dir.create(file.path(projectFolderName), showWarnings = FALSE, recursive = TRUE)
    for (id in unique(data[["id"]])) {

      # print prediction for project
      plotPath <- paste(projectFolderName, paste0(id, ".png"), sep = "/")
      png(plotPath, width = 15, height = 10, units = "cm", res = 300, , pointsize = 8)
      plot_project(model, id, data)
      dev.off()
    }
  }
}


# ------------------------------------------------------------------------------


