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

    # Load Data
    dataNotClean <- read.csv(fileName)
    data <- dataNotClean[complete.cases(dataNotClean), ]

    # Create model
    model <- lmer(
      as.formula(paste0(metric, " ~ 1 + t_pre + t_post + intervention + (1 + t_pre + t_post + intervention | id)")),
      data = data
    )

    # print model results
    capture.output(summary(model), file = file.path(folderName, "model_summary.txt"))
    capture.output(VarCorr(model), file = file.path(folderName, "model_var_corr.txt"))

    # print metric histogram
    plotPath <- paste(folderName, "hist.png", sep = "/")
    png(plotPath, width = 1600, height = 1000)

    d <- data[[metric]]
    sigma <- sd(d)
    mean <- mean(d)
    hist(
      d[d <= mean + 3 * sigma],
      breaks = 100,
      freq = FALSE,
      main = "Histogram",
      xlab = metric,
      col = "lightblue"
    )
    dev.off()

    # print fixed effects
    plotPath <- paste(folderName, "predicted.png", sep = "/")
    png(plotPath, width = 1600, height = 1000)
    plot_predicted(data, model)
    dev.off()

    # print qq of residuals
    plotPath <- paste(folderName, "qq.png", sep = "/")
    png(plotPath, width = 1600, height = 1000)
    plot_qq(data, model)
    dev.off()

    # print residuals vs fitted
    plotPath <- paste(folderName, "residuals_vs_fitted.png", sep = "/")
    png(plotPath, width = 1600, height = 1000)
    plot_residuals_vs_fitted(data, model)
    dev.off()

    # print residuals histogram
    plotPath <- paste(folderName, "residuals_hist.png", sep = "/")
    png(plotPath, width = 1600, height = 1000)
    plot_residuals_histogram(data, model)
    dev.off()

    # print random effects histograms
    random_effects <- ranef(model)[[1]]
    fixed_effects <- fixef(model)
    absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

    plotPath <- paste(folderName, "random_effects_hist.png", sep = "/")
    png(plotPath, width = 1600, height = 1000)
    par(mfrow = c(2, 2))
    plot_random_effects_histogram(absolute_effects$t_pre, "lightgreen", "t_pre")
    plot_random_effects_histogram(absolute_effects$t_post, "lightcoral", "t_post")
    plot_random_effects_histogram(absolute_effects$intervention, "cornsilk", "intervention")
    plot_random_effects_histogram(absolute_effects$`(Intercept)`, "lightblue", "intercept")
    dev.off()
    par(mfrow = c(1, 1))

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





# ------------------------------------------------------------------------------

random_effects <- ranef(model)[[1]]
fixed_effects <- fixef(model)
absolute_effects <- sweep(random_effects, MARGIN = 2, STATS = fixed_effects, FUN = "+")

# gepaarter t-test
t.test(absolute_effects$t_pre, absolute_effects$t_post, paired = TRUE)


# ------------------------------------------------------------------------------

project <- "abrensch.brouter"
randomEffectsFromProject = ranef(model)$id[project,]
fixedEffects <- fixef(model)
randomEffectsFromProject <- fixedEffects + randomEffectsFromProject

# compute predicted values
x <- seq(-2, 2, by = 0.01)
predictedData <- data.frame(
  x = x,
  y = ifelse(
    x < 0,
    randomEffectsFromProject$`(Intercept)` + randomEffectsFromProject$t_pre * x,
    randomEffectsFromProject$`(Intercept)` + randomEffectsFromProject$intervention + randomEffectsFromProject$t_post * x
  )
)

# scatter plot data
projectData <- data[data$id == project, ]
projectData <- projectData[c(metric, "t_pre", "t_post")]
projectData <- data.frame(
  time = ifelse(
    projectData$t_pre != 0,
    projectData$t_pre,
    projectData$t_post
  ),
  metric = projectData[[metric]]
)


# limits
ymin_all <- min(c(predictedData$y, projectData$metric))
ymax_all <- max(c(predictedData$y, projectData$metric))
ydiff_all <- ymax_all - ymin_all
upper_limit <- ymax_all + ydiff_all / 5
lower_limit <- ymin_all - ydiff_all / 5



# predicted graph
plot(
  predictedData$x, predictedData$y,
  type = "l", # 'l' for line
  xlab = "Time in years",
  ylab = paste("Metric:", metric),
  main = paste("Project:", project),
  xlim = c(-2, 2),
  ylim = c(lower_limit, upper_limit),
  lwd = 2, # Line width
  col = "blue" # Line color
)

# scatter
points(
  projectData$time, projectData$metric,
)

# intervention line
abline(v = 0, lty = 2, col = "grey")

# legend
legend("topright",
       legend = c("Predicted Trend", "Actual Data"),
       col = c("blue", "red"),
       lty = c(1, NA), # Line for trend, no line for data
       pch = c(NA, 19)) # No symbol for trend, circle for data





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

