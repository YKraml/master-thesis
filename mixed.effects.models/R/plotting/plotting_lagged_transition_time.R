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
       ylab = "Value")

  grid(nx = NULL, ny = NULL, col = "lightgray", lty = "dotted")
}

# ------------------------------------------------------------------------------



# ------------------------------------------------------------------------------

plot_max_abs_normalized_change <- function(effect_name, x_values, y_values, color) {

  # 1. Calculate the maximum absolute distance from 0
  # This finds the point furthest from zero, regardless of direction
  max_abs_val <- max(abs(y_values), na.rm = TRUE)

  # Handle edge case where all values are 0
  if (max_abs_val == 0) {
    data_range <- 1
  } else {
    data_range <- max_abs_val
  }

  # 2. Calculate normalized change
  # The change is now expressed as a percentage of the peak magnitude
  n <- length(y_values)
  norm_change <- ((y_values[-1] - y_values[-n]) / data_range) * 100

  # X-coordinates for the midpoints
  dx <- diff(x_values)
  x_rel <- x_values[-n] + (dx / 2)

  # 3. Set y-limits with a small buffer
  y_limits <- range(c(norm_change, 0), na.rm = TRUE)
  range_span <- diff(y_limits)
  if(range_span == 0) range_span <- 1

  y_limits[1] <- y_limits[1] - (range_span * 0.1)
  y_limits[2] <- y_limits[2] + (range_span * 0.1)

  # 4. Plotting
  plot(x_rel, norm_change,
       type = "o",
       pch = 20,
       col = color,
       ylim = y_limits,
       main = effect_name,
       xlab = "Transition Time (Days)",
       ylab = "Relative Change (%)")

  # Overlay red points for significant changes
  point_colors <- ifelse(abs(norm_change) > 5, "red", color)
  points(x_rel, norm_change, pch = 20, col = point_colors)

  grid(nx = NULL, ny = NULL, col = "lightgray", lty = "dotted")
}

# ------------------------------------------------------------------------------
