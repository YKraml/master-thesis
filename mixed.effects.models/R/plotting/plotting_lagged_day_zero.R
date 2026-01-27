# ------------------------------------------------------------------------------

plot_lag_comparison <- function(results_df) {

  # Sort by lag (Crucial for base R lines to connect correctly)
  results_df <- results_df[order(results_df$lag), ]

  # Find Best Fit row (Min AIC)
  min_aic <- min(results_df$AIC, na.rm = TRUE)
  best_model <- results_df[results_df$AIC == min_aic, ][1, ]

  # Setup the canvas
  y_max <- max(results_df$AIC, na.rm = TRUE)
  y_min <- min(results_df$AIC, na.rm = TRUE)

  plot(x = results_df$lag,
       y = results_df$AIC,
       main = "Model Fit vs Time Shift",
       xlab = "Lag (Weeks)",
       ylab = "AIC Score",
       ylim = c(y_min, y_max))

  # Add grid
  grid(nx = NULL, ny = NULL, col = "lightgray", lty = "dotted")

  # Vertical line at 0 (Intervention)
  abline(v = 0, col = "grey", lty = "dashed", lwd = 1.5)

  # Draw Line and Points (Main trend)
  lines(results_df$lag, results_df$AIC, col = "steelblue", lwd = 2)
  points(results_df$lag, results_df$AIC, col = "steelblue", pch = 19)

  # Highlight the Global Winner (Best Fit)
  points(best_model$lag, best_model$AIC, col = "red", pch = 19, cex = 1.5)

  # Add Legend
  legend(
    "topright",
    legend = c("AIC Trend", "Intervention", "Best Fit"),
    col = c("steelblue", "grey", "red"),
    lty = c(1, 2, NA),
    pch = c(19, NA, 19),
    lwd = c(2, 1.5, NA)
  )
}

# ------------------------------------------------------------------------------
