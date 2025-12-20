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

# ------------------------------------------------------------------------------

plot_residuals_vs_time_1 <- function(data, model) {
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

# ------------------------------------------------------------------------------

plot_residuals_vs_time_2 <- function(data, model) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)

  plot(
    data$t_post,
    residuals,
    xlab = "Time in years",
    ylab = "Residuals",
    main = "Residuals vs time"
  )

  abline(h = 0, col = "red", lty = 2) # Add a horizontal line at 0
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_residuals_vs_fitted <- function(data, model) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)

  plot(
    fitted_vals,
    residuals,
    xlab = "Fitted values",
    ylab = "Residuals",
    main = "Residuals vs fitted"
  )
  abline(h = 0, col = "red", lty = 2) # Add a horizontal line at 0
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_residuals_qq <- function(data, model) {
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
