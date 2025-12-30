# ------------------------------------------------------------------------------

plot_predicted_1 <- function(data, model) {

  # Extract fixed effects and their covariance
  fixed_effects  <- fixef(model)
  covariance    <- vcov(model)

  # Create new time sequence
  pred <- data.frame(
    x <- seq(-2, 2, by = 0.01)
  )

  # Build new dataframe
  pred$intervention = ifelse(pred$x < 0, 0, 1)
  pred$t_pre  <- pred$x * (1 - pred$intervention)
  pred$t_post <- pred$x * pred$intervention

  # Create the model matrix for fixed effects
  X <- model.matrix(~ 1 + t_pre + t_post + intervention, data = pred)

  # Predicted values (fixed effects only)
  pred$pred <- X %*% fixed_effects

  # Standard error of predictions
  pred$se <- sqrt(diag(X %*% covariance %*% t(X)))

  # Confidence intervals (95%)
  pred$lower <- pred$pred - 1.96 * pred$se
  pred$upper <- pred$pred + 1.96 * pred$se


  plot_predicted(pred$x, pred$pred, pred$lower, pred$upper)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_predicted_2 <- function(data, model){
  pred <- ggpredict(model, terms = "t_post [0:2 by=0.01]")
  plot_predicted(pred$x, pred$predicted, pred$conf.low, pred$conf.high)
}

# ------------------------------------------------------------------------------

plot_predicted <- function(x, y, conf_low, conf_high){

  # Calculate y range
  ymin <- min(conf_low)
  ymax <- max(conf_high)
  yrange <- ymax - ymin
  ymin2 <- ymin - yrange / 2
  ymax2 <- ymax + yrange / 2

  # plot graph
  plot(
    x, y, type = "l",
    xlab = "Time in years", ylab = "Predicted metric",
    ylim = c(ymin2, ymax2),
    col = "red",
    main = "Fitted fixed effect with 95% confidence interval"
  )

  # conf int
  lines(x, conf_low, col = "lightblue", lty = 1)
  lines(x, conf_high, col = "lightblue", lty = 1)

  # intervention line
  abline(v = 0, lty = 2, col = "darkgrey")

  # legend
  legend("topright",
         legend = c("Fitted", "95% Confidence Interval", "Linter Introduction"),
         col = c("red", "lightblue", "darkgrey"),
         lty = c(1, 1, 2),
         pch = c(NA, NA, NA))
}
