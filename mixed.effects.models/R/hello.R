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

data <- read.csv('data/CHECKSTYLE.averageFileLoc.csv')
hist(
  data$averageFileLoc,
  breaks = 1000,
  main = "Histogram of Relative Number of File Size Violations",
  xlab = "Relative Number of File Size Violations"
)


model <- lme(
  fixed = averageFileLoc ~ 1 + t_pre + t_post + intervention,
  data = data,
  random = ~ 1 + t_pre + t_post + intervention | id,
  method = "ML"
)

summary(model)
plot(ranef(model))
VarCorr(model)

residuals <- residuals(model)
fitted_vals <- fitted(model)
residual_data <- data.frame(Fitted = fitted_vals, Residuals = residuals)

# Base R Q-Q Plot
qqnorm(residuals, main = "Q-Q Plot of Standardized Residuals")
qqline(residuals, col = "red")

# Base R Residuals Plot
plot(fitted_vals, residuals,
     xlab = "Fitted Values",
     ylab = "Standardized Residuals",
     main = "Residuals vs. Fitted Values Plot")
abline(h = 0, col = "red", lty = 2) # Add a horizontal line at 0

# Residuals histogram
hist(residuals,
     main = "Histogram of Standardized Residuals",
     xlab = "Standardized Residuals",
     ylab = "Density",
     col = "lightblue",
     border = "white",
     freq = FALSE,
     breaks = 5000, # Use more breaks for better resolution
     xlim = c(-10, 10)
)

curve(dnorm(x, mean=mean(residuals), sd=sd(residuals)),
      col="red",
      lwd=2,
      add=TRUE) # 'add=TRUE' overlays the curve onto the existing plot

legend("topright",
       legend = "Normal Density Curve",
       col = "red",
       lwd = 2,
       bty = "n")

newdat <- expand.grid(Sex=unique(Orthodont$Sex),
                      age=c(min(Orthodont$age),
                            max(Orthodont$age)))



# Plotting the effect of 't_pre'
plot(effect("t_pre", model),
     main = "Effect of t_pre",
     xlab = "T_Pre",
     ylab = "Predicted averageFileLoc")

# Plotting the effect of 't_post'
plot(effect("t_post", model),
     main = "Effect of t_post",
     xlab = "T_Post",
     ylab = "Predicted averageFileLoc")

# Plotting the effect of 'intervention'
plot(effect("intervention", model),
     main = "Effect of Intervention",
     xlab = "Intervention Group",
     ylab = "Predicted averageFileLoc")

