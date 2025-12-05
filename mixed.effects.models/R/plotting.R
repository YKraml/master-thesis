# ------------------------------------------------------------------------------

plot_predicted <- function(data, model) {

  # Extract fixed effects and their covariance
  beta  <- fixef(model)
  Vb    <- vcov(model)

  # Create new time sequence
  new_time <- seq(-2, 2, by = 0.01)

  # Build new dataframe
  newdata <- data.frame(
    time = new_time,
    intervention = ifelse(new_time < 0, 0, 1)
  )

  # Construct predictors exactly as in your model
  newdata$t_pre  <- newdata$time * (1 - newdata$intervention)
  newdata$t_post <- newdata$time * newdata$intervention

  # Create the model matrix for fixed effects
  X <- model.matrix(~ 1 + t_pre + t_post + intervention, data = newdata)

  # Predicted values (fixed effects only)
  newdata$pred <- X %*% beta

  # Standard error of predictions
  newdata$se <- sqrt(diag(X %*% Vb %*% t(X)))

  # Confidence intervals (95%)
  newdata$lower <- newdata$pred - 1.96 * newdata$se
  newdata$upper <- newdata$pred + 1.96 * newdata$se

  # Calculate current y-range
  ymin <- min(newdata$lower)   # CI lower bound
  ymax <- max(newdata$upper)   # CI upper bound

  # Expand by a factor of 2
  yrange <- ymax - ymin
  ymin2 <- ymin - yrange / 2
  ymax2 <- ymax + yrange / 2

  # ---- Plot with confidence band ----
  plot(
    newdata$time, newdata$pred, type = "l",
    xlab = "Time in years", ylab = "Predicted metric",
    main = "Predicted metric with 95% CI",
    xlim = c(-2, 2),
    ylim = c(ymin2, ymax2)
  )

  # Add confidence interval ribbon
  polygon(
    c(newdata$time, rev(newdata$time)),
    c(newdata$upper, rev(newdata$lower)),
    col = adjustcolor("gray70", alpha.f = 0.5),
    border = NA
  )
}

# ------------------------------------------------------------------------------

plot_qq <- function(data, model) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)
  residual_data <- data.frame(Fitted = fitted_vals, Residuals = residuals)

  # Base R Q-Q Plot
  qqnorm(residuals, main = "Q-Q Plot of Standardized Residuals")
  qqline(residuals, col = "red")
}

# ------------------------------------------------------------------------------

plot_residuals_vs_fitted <- function(data, model) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)

  # Base R Residuals Plot
  plot(fitted_vals, residuals,
       xlab = "Fitted Values",
       ylab = "Residuals",
       main = "Residuals vs. Fitted Values Plot")
  abline(h = 0, col = "red", lty = 2) # Add a horizontal line at 0
}

# ------------------------------------------------------------------------------

plot_residuals_histogram <- function(data, model) {
  residuals <- residuals(model)

  sigma <- sd(residuals)
  mean <- mean(residuals)
  left = mean - sigma * 2
  right = mean + sigma * 2

  # Residuals histogram
  hist(
    residuals[residuals > left & residuals < right],
    main = "Histogram of Standardized Residuals",
    xlab = "Standardized Residuals",
    ylab = "Density",
    col = "lightblue",
    border = "white",
    freq = FALSE,
    breaks = 50,
    xlim = c(left, right)
  )

  curve(
    dnorm(x, mean=mean(residuals), sd=sd(residuals)),
    col = "red",
    lwd = 2,
    add = TRUE
    )
}

# ------------------------------------------------------------------------------

plot_random_effects_histogram <- function(data, color, main) {
  sigma <- sd(data)
  mean <- mean(data)
  left = mean - sigma * 2
  right = mean + sigma * 2
  hist(
    data[data > left & data < right],
    main = main,
    xlab = "Value",
    ylab = "Density",
    freq = FALSE,
    col = color,
    breaks = 50,
    xlim = c(left, right)
  )
  abline(v = mean, col = "blue", lwd = 2, lty = 2)
  text(
    x = mean,
    y = max(hist(data, breaks = 100, plot = FALSE)$density) * 0.9,
    labels = paste("fixed =", round(mean, 3)),
    pos = 4,
    col = "blue",
    font = 2
  )
}

# ------------------------------------------------------------------------------

plot_project <- function(model, project, data) {
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

  # get introduction date
  l <- data[data$id == project, ]
  seconds_timestamp <- l[l$intervention == 1, ][1, ]$time
  date_time_object <- as.POSIXct(seconds_timestamp, origin = "1970-01-01", tz = "GMT")
  date <- format(date_time_object, format = "%d.%m.%Y")


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
    col = "lightcoral" # Line color
  )

  # scatter
  points(
    projectData$time, projectData$metric,
    col = "lightblue"
  )

  # intervention line
  abline(v = 0, lty = 2, col = "darkgrey")


  # legend
  legend("topright",
         legend = c("Predicted Trend", "Actual Data", paste0("Introduction: ", date)),
         col = c("lightcoral", "lightblue", "darkgrey"),
         lty = c(1, NA, 2), # Line for trend, no line for data
         pch = c(NA, 19, NA)) # No symbol for trend, circle for data
}


