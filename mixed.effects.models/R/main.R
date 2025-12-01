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

source("R/plotting.R")


fileNames <- list.files("data", pattern="*.csv", full.names=TRUE, recursive = TRUE)
metrics <- list(
  "relativeNumberOfCodeClones",
  "relativeNumberOfFileSizeViolations",
  "relativeNumberOfMethodLengthViolations",
  "relativeNumberOfNestingDepthViolations",
  "averageFileLoc"
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

    # print current loop iteration


    # Load Data
    dataNotClean <- read.csv(fileName)
    data <- dataNotClean[complete.cases(dataNotClean), ]

    # Create model
    model <- lme(
      fixed = as.formula(
        paste0(metric, " ~ 1 + t_pre + t_post + intervention")
      ),
      data = data,
      random = ~ 1 + t_pre + t_post + intervention | id,
      method = "ML",
      control = lmeControl(opt = "optim")
    )

    # model <- lmer(
    #   as.formula(paste0(metric, " ~ 1 + t_pre + t_post + intervention + (1 + t_pre + t_post + intervention | id)")),
    #   data = data,
    #   REML = FALSE
    # )

    # print model results
    capture.output(summary(model), file = file.path(folderName, "model_summary.txt"))
    capture.output(VarCorr(model), file = file.path(folderName, "model_var_corr.txt"))

    # print metric histogram
    plotPath <- paste(folderName, "hist.png", sep = "/")
    png(plotPath, width = 1000, height = 1000)
    histPlot <- hist(
      data[[metric]],
      breaks = 1000,
      main = "Histograms",
      xlab = "Metric"
    )
    dev.off()

    # print random effects
    re <- ranef(model)$id %>%
      tibble::rownames_to_column("group") %>%
      gather(effect, value, -group)
    plotPath <- paste(folderName, "ranef.png", sep = "/")
    png(plotPath, width = 1000, height = 1000)
    plot(
      ranef(model),
      scales = list(y = list(draw = FALSE))   # hides y-axis labels
    )
    dev.off()

    # print fixed effects
    plotPath <- paste(folderName, "predicted.png", sep = "/")
    png(plotPath, width = 1000, height = 1000)
    plot_predicted(data, model)
    dev.off()

    # print qq of residuals
    plotPath <- paste(folderName, "qq.png", sep = "/")
    png(plotPath, width = 1000, height = 1000)
    plot_qq(data, model)
    dev.off()

    # print residuals vs fitted
    plotPath <- paste(folderName, "residuals_vs_fitted.png", sep = "/")
    png(plotPath, width = 1000, height = 1000)
    plot_residuals_vs_fitted(data, model)
    dev.off()

    # print residuals histogram
    plotPath <- paste(folderName, "residuals_hist.png", sep = "/")
    png(plotPath, width = 1000, height = 1000)
    plot_residuals_histogram(data, model)
    dev.off()
  }
}


dataNotClean <- read.csv('data/bucket_one.CHECKSTYLE.csv')
data <- dataNotClean[complete.cases(dataNotClean), ]


model <- lme(
  fixed = averageFileLoc ~ 1 + t_pre + t_post + intervention,
  data = data,
  random = ~ 1 + t_pre + t_post + intervention | id,
  method = "ML",
  control = lmeControl(opt = "optim")
)

summary(model)
VarCorr(model)

plot(ranef(model))
plot_predicted(data, model)
plot_qq(data, model)
plot_residuals_vs_fitted(data, model)
plot_residuals_histogram(data, model)









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

