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

dataNotClean <- read.csv('data/bucket_one.CHECKSTYLE.csv')
data <- dataNotClean[complete.cases(dataNotClean), ]


hist(
  data$averageFileLoc,
  breaks = 1000,
  main = "Histograms",
  xlab = "Metric"
)


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

