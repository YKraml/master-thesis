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
  # Residuals histogram
  hist(residuals,
       main = "Histogram of Standardized Residuals",
       xlab = "Standardized Residuals",
       ylab = "Density",
       col = "lightblue",
       border = "white",
       freq = FALSE,
       breaks = 5000,
       xlim = c(-10, 10)
  )

  curve(dnorm(x, mean=mean(residuals), sd=sd(residuals)),
        col = "red",
        lwd = 2,
        add = TRUE)
}

# ------------------------------------------------------------------------------

plot_random_effects_histogram <- function(data, color, main) {
  sigma <- sd(data)
  mean <- mean(data)
  hist(
    data,
    main = main,
    xlab = "Value",
    ylab = "Density",
    freq = FALSE,
    col = color,
    border = "black",
    breaks = 100,
    xlim = c(mean - sigma * 2, mean + sigma * 2)
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

