# ------------------------------------------------------------------------------

plot_residuals_histogram <- function(data, model, color) {
  residuals <- residuals(model)

  sigma <- sd(residuals)
  mean <- mean(residuals)
  left = mean - sigma * 3
  right = mean + sigma * 3

  # Residuals histogram
  hist(
    residuals[residuals > left & residuals < right],
    xlab = "Residuals",
    ylab = "Density",
    col = color,
    border = "black",
    freq = FALSE,
    breaks = 30,
    xlim = c(left, right),
    main = "Histogram of Residuals"
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

plot_residuals_vs_time <- function(data, model, color) {

  residuals <- residuals(model)
  time <- data$base_slope


  plot(
    time,
    residuals,
    xlab = "Time (Years)",
    ylab = "Residuals",
    main = "Residuals vs Time",
    col = color,
    pch = 4
  )

  abline(h = 0, col = "red", lty = 2)
}

# ------------------------------------------------------------------------------


plot_residuals_vs_age <- function(data, model, color) {
  residuals <- residuals(model)
  time <- data$relative_introduction_time


  plot(
    time,
    residuals,
    xlab = "Age at Linter Introduction (Years)",
    ylab = "Residuals",
    main = "Residuals vs Age",
    col = color,
    pch = 4
  )

  abline(h = 0, col = "red", lty = 2)
}

# ------------------------------------------------------------------------------

plot_residuals_vs_fitted <- function(data, model, color, metric_display_name, metric_unit) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)

  plot(
    fitted_vals,
    residuals,
    xlab = sprintf("%s (%s)", metric_display_name, metric_unit),
    ylab = "Residuals",
    main = "Residuals vs Fitted",
    col = color,
    pch = 4
  )

  abline(h = 0, col = "red", lty = 2)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_residuals_qq <- function(data, model) {
  residuals <- residuals(model)
  fitted_vals <- fitted(model)
  residual_data <- data.frame(Fitted = fitted_vals, Residuals = residuals)

  qqnorm(residuals)
  qqline(
    residuals,
    col = "red"
  )

  grid()
}

# ------------------------------------------------------------------------------
