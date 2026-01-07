# ------------------------------------------------------------------------------

# Helper function to format p-values nicely
fmt_p <- function(p) {
  if (p < 0.001) return("< 0.001")
  return(paste0("= ", format(round(p, 3), nsmall = 3)))
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_predicted_1 <- function(data, model, title = "Fitted fixed effect with 95% confidence interval") {

  # Extract fixed effects and their covariance
  fixed_effects  <- fixef(model)
  covariance    <- vcov(model)
  t_table <- summary(model)$tTable
  p_values <- t_table[, "p-value"]



  # Create new time sequence
  pred <- data.frame(
    x <- seq(-2, 2, by = 0.01)
  )

  # Build new dataframe
  pred$intervention_jump = ifelse(pred$x < 0, 0, 1)
  pred$base_slope  <- pred$x
  pred$intervention_slope <- pred$x * pred$intervention_jump

  # Create the model matrix for fixed effects
  X <- model.matrix(~ 1 + base_slope + intervention_slope + intervention_jump, data = pred)

  # Predicted values (fixed effects only)
  pred$pred <- X %*% fixed_effects

  # Standard error of predictions
  pred$se <- sqrt(diag(X %*% covariance %*% t(X)))

  # Confidence intervals (95%)
  pred$lower <- pred$pred - 1.96 * pred$se
  pred$upper <- pred$pred + 1.96 * pred$se

  # text in the corner
  text <- paste0(
    "intercept = ", round(fixed_effects["(Intercept)"], 3),
    " (p ", fmt_p(p_values["(Intercept)"]), ")\n",
    "base_slope = ", round(fixed_effects["base_slope"], 3),
    " (p ", fmt_p(p_values["base_slope"]), ")\n",
    "intervention_jump = ", round(fixed_effects["intervention_jump"], 3),
    " (p ", fmt_p(p_values["intervention_jump"]), ")\n",
    "intervention_slope = ", round(fixed_effects["intervention_slope"], 3),
    " (p ", fmt_p(p_values["intervention_slope"]), ")\n"
  )

  plot_predicted(pred$x, pred$pred, pred$lower, pred$upper, text, title)
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_predicted_2 <- function(data, model){
  fixed_effects  <- fixef(model)
  t_table <- summary(model)$tTable
  p_values <- t_table[, "p-value"]

  # text in the corner
  text <- paste0(
    "intercept = ", round(fixed_effects["(Intercept)"], 3),
    " (p ", fmt_p(p_values["(Intercept)"]), ")\n",
    "base_slope = ", round(fixed_effects["slope"], 3),
    " (p ", fmt_p(p_values["slope"])
    )

  pred <- ggpredict(model, terms = "slope [0:2 by=0.01]")
  plot_predicted(pred$x, pred$predicted, pred$conf.low, pred$conf.high, text)
}

# ------------------------------------------------------------------------------

plot_predicted <- function(x, y, conf_low, conf_high, text = "", title = "Fitted fixed effect with 95% confidence interval"){

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
    main = title
  )

  text(x = x[2], y = ymax2, labels = text, adj = c(0, 1))

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
