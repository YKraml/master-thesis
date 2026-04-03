# ------------------------------------------------------------------------------

plot_lagged <- function(effect_name, x_values, y_values, color){

  # 1. Calculate the first derivative (raw values)
  dy <- diff(y_values)
  dx <- diff(x_values)
  derivative <- dy / dx
  x_deriv <- x_values[-length(x_values)] + (dx / 2)

  # 2. Set y-limits to encompass both datasets
  # This will likely span from ~0 to ~120 based on your image
  y_limits <- range(c(y_values, derivative, 0), na.rm = TRUE)

  # Add a 10% buffer for visual breathing room
  range_span <- diff(y_limits)
  y_limits[1] <- y_limits[1] - (range_span * 0.1)
  y_limits[2] <- y_limits[2] + (range_span * 0.1)

  # 3. Plot the original data
  plot(x_values, y_values,
       type = "o",
       pch = 20,
       col = color,
       ylim = y_limits,
       main = effect_name,
       xlab = "Transition Time (Days)",
       ylab = "Value",
       cex.main = 1)

  grid(nx = NULL, ny = NULL, col = "lightgray", lty = "dotted")

  # 4. Add the raw derivative line
  lines(x_deriv, derivative, col = "darkgrey", lty = "dashed", lwd = 2)

  # 5. Add Legend
  legend("right",
         legend = c("Estimate", "1st Derivative"),
         col = c(color, "darkgrey"),
         lty = c(1, 2),
         pch = c(20, NA),
         cex = 0.8)
}

# ------------------------------------------------------------------------------
