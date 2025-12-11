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
  for (metric in metrics) {
    print(paste(" -",metric))

    # Create folder
    cleanedFileName <- gsub("data.", "", fileName)
    cleanedFileName <- gsub(".csv", "", cleanedFileName)
    folderName <- paste("plots",cleanedFileName, metric, sep = "/")
    dir.create(file.path(folderName), showWarnings = FALSE, recursive = TRUE)

    # Load Data
    dataNotClean <- read.csv(fileName)
    data <- dataNotClean[complete.cases(dataNotClean), ]

    # Create model
    model <- lmer(
      as.formula(paste0(metric, " ~ 1 + t_pre + t_post + intervention + (1 + t_pre + t_post + intervention | id)")),
      data = data
    )

    random_effects <- ranef(model)[[1]]
    fixed_effects <- fixef(model)
    absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

    # gepaarter t-test


    # print model results
    capture.output(summary(model), file = file.path(folderName, "model_summary.txt"))
    capture.output(VarCorr(model), file = file.path(folderName, "model_var_corr.txt"))

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

    # print random effects histograms
    random_effects <- ranef(model)[[1]]
    fixed_effects <- fixef(model)
    absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

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

dataNotClean <- read.csv('data/bucket_one/CHECKSTYLE.csv')
data <- dataNotClean[complete.cases(dataNotClean), ]
metric <- "averageFileLoc"

model <- lmer(
  as.formula(paste0(metric," ~ 1 + t_pre + t_post + intervention + (1 + t_pre + t_post + intervention | id)")),
  data = data
)

summary(model)
VarCorr(model)

source("R/plotting.R")
plot_project(model, "apache.commons-io", data)

# ------------------------------------------------------------------------------

random_effects <- ranef(model)[[1]]
fixed_effects <- fixef(model)
absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

# gepaarter t-test
t.test(absolute_effects$t_pre, absolute_effects$t_post, paired = TRUE)


# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------

plot_predicted(data, model)
plot_qq(data, model)
plot_residuals_vs_fitted(data, model)
plot_residuals_histogram(data, model)

# ------------------------------------------------------------------------------


# Plotting the effect of 't_pre'
plot(effect("t_pre", model),
     main = "Effect of t_pre",
     xlab = "T_Pre",
     ylab = "Predicted averageFileLoc")

# Plotting the effect of 't_post'
plot(effect("t_post", model),
     main = "Effect of t_post",
     xlab = "T_Post",
     ylab = "Predicted averageFileLoc",)

# Plotting the effect of 'intervention'
plot(effect("intervention", model),
     main = "Effect of Intervention",
     xlab = "Intervention Group",
     ylab = "Predicted averageFileLoc")

# ------------------------------------------------------------------------------

