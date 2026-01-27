# ------------------------------------------------------------------------------

plot_lagged <- function(effect_name, x_values, y_values){

  y_limits <- range(c(0, y_values))

  # 10% buffer at the top and bottom
  range_span <- diff(y_limits)
  y_limits[1] <- y_limits[1] - (range_span * 0.1)
  y_limits[2] <- y_limits[2] + (range_span * 0.1)

  plot(x_values, y_values,
       type = "o",
       pch = 20,
       col = "red",
       ylim = y_limits,
       main = effect_name,
       xlab = "Transition Time (Days)",
       ylab = "Estimate",
       cex.main = 1)

  grid(nx = NULL, ny = NULL, col = "lightgray", lty = "dotted")

}

# ------------------------------------------------------------------------------
