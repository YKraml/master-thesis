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
    xlim = c(-2, 2),
    ylim = c(ymin2, ymax2),
    main = "Fitted fixed effect with 95% confidence interval"
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
  qqnorm(residuals)
  qqline(
    residuals,
    col = "red"
  )
}

# ------------------------------------------------------------------------------

plot_residuals_vs_fitted <- function(data, model) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)

  plot(
    fitted_vals, residuals,
    xlab = "Fitted values",
    ylab = "Residuals",
    main = "Residuals vs fitted"
  )
  abline(h = 0, col = "red", lty = 2) # Add a horizontal line at 0
}

# ------------------------------------------------------------------------------

plot_residuals_vs_time <- function(data, model) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)

  plot(
    data$cleaned_time,
    residuals,
    xlab = "Time in years",
    ylab = "Residuals",
    main = "Residuals vs time"
  )
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
    xlab = "Residuals",
    ylab = "Density",
    col = "lightblue",
    border = "black",
    freq = FALSE,
    breaks = 30,
    xlim = c(left, right),
    main = "Hisotgram of residuals"
  )

  curve(
    dnorm(x, mean=mean(residuals), sd=sd(residuals)),
    col = "red",
    lwd = 2,
    add = TRUE
  )

  abline(v = mean, col = "red", lwd = 2, lty = 2)
}

# ------------------------------------------------------------------------------

plot_random_effects_histogram <- function(data, color, main, fixed) {
  sigma <- sd(data)
  mean <- mean(data)
  left = mean - sigma * 2
  right = mean + sigma * 2
  hist(
    data[data > left & data < right],
    main = main,
    xlab = "Value",
    ylab = "Density",
    freq = TRUE,
    col = color,
    border = "black",
    breaks = 30,
    xlim = c(left, right)
  )
  abline(v = mean, col = "red", lwd = 2, lty = 2)

  # legend
  legend(
    "topright",
    legend = c(paste0("Mean: ", round(mean, 3)), paste0("Fixed: ", round(fixed, 3))),
    col = c("red", NA),
    lty = c(2, NA),
    pch = c(NA, NA))
}

# ------------------------------------------------------------------------------

plot_project <- function(model, id, data) {
  random_effects <- ranef(model)
  randomEffectsFromProject = random_effects[id,]
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
  projectData <- data[data$id == id, ]
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
  upper_limit <- ymax_all + ydiff_all / 2
  lower_limit <- ymin_all - ydiff_all / 2

  # get introduction date
  l <- data[data$id == id, ]
  seconds_timestamp <- l[l$intervention == 1, ][1, ]$time
  date_time_object <- as.POSIXct(seconds_timestamp, origin = "1970-01-01", tz = "GMT")
  date <- format(date_time_object, format = "%d.%m.%Y")


  # predicted graph
  plot(
    predictedData$x, predictedData$y,
    type = "l",
    xlab = "Time in years",
    ylab = paste("Metric:", metric),
    xlim = c(-2, 2),
    ylim = c(lower_limit, upper_limit),
    lwd = 2,
    col = "red",
    main = paste0("Project: ", id)
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
         legend = c("Fitted", "Actual data", paste0("Introduction: ", date)),
         col = c("red", "lightblue", "darkgrey"),
         lty = c(1, NA, 2),
         pch = c(NA, 1, NA))
}

# ------------------------------------------------------------------------------

plot_raw_data_hist <- function(data, metric){
  sigma <- sd(data)
  mean <- mean(data)
  hist(
    data[data <= mean + 3 * sigma],
    breaks = 30,
    freq = TRUE,
    xlab = metric,
    col = "lightblue",
    border = "black",
    main = paste0("Histogram of: ", metric),
    xlim = c(0, mean + 3 * sigma)
  )
}

# ------------------------------------------------------------------------------

plot_random_effects_vs_residuals <- function(data, model, effect) {
  # Residuals
  resid_df <- data.frame(
    id = data$id,
    residual = resid(model, type = "pearson")
  )

  # Random effects
  ranef_df <- tibble::rownames_to_column(ranef(model), "id")
  plot_df <- merge(resid_df, ranef_df, by = "id")

  plot_df$id <- factor(plot_df$id)
  ggplot(plot_df, aes(x = !!sym(effect), y = residual, color = id)) +
    geom_point() +
    labs(
      x = paste0("Random effect of ", effect, " (by id)"),
      y = "Residual",
      title = paste0("Residuals vs Random Effect of ", effect)
    ) +
    theme(legend.position = "none")
}
