

# ------------------------------------------------------------------------------

plot_raw_data_hist <- function(data, metric, metric_unit){
  sigma <- sd(data)
  mean <- mean(data)
  hist(
    data[data <= mean + 3 * sigma],
    breaks = 30,
    freq = TRUE,
    xlab = sprintf("%s (%s)", metric, metric_unit),
    col = "lightblue",
    border = "black",
    main = "Metric at Linter Introduction",
    xlim = c(0, mean + 3 * sigma)
  )
}

# ------------------------------------------------------------------------------


plot_all_projects_hexbin <- function(data, metric, metric_unit, metric_display_name, title){

  # 1. Prepare data
  plot_df <- data.frame(
    time = ifelse(data$base_slope != 0, data$base_slope, data$slope_change),
    metric_val = data[[metric]]
  )
  plot_df <- na.omit(plot_df)

  # Sort by time (required for smooth.spline to work perfectly)
  plot_df <- plot_df[order(plot_df$time), ]

  # 2. Smart Zoom Limits
  y_quants <- quantile(plot_df$metric_val, probs = c(0.2, 0.8))
  ymin_zoom <- y_quants[1]
  ymax_zoom <- y_quants[2]
  buffer <- (ymax_zoom - ymin_zoom) * 0.1
  lower_limit <- ymin_zoom - buffer
  upper_limit <- ymax_zoom + buffer

  # Calculate proportional bin widths
  x_range <- max(plot_df$time) - min(plot_df$time)
  y_range <- upper_limit - lower_limit
  my_binwidth <- c(x_range / 50, y_range / 50)

  # 3. --- NEW: Calculate the exact Base R Spline ---
  # We check if we have enough points, just like your original code
  if(nrow(plot_df) >= 4) {
    spline_fit <- smooth.spline(plot_df$time, plot_df$metric_val, spar = 0.7)
    # Convert the spline output into a dataframe so ggplot can read it
    spline_df <- data.frame(time = spline_fit$x, metric_val = spline_fit$y)
  } else {
    spline_df <- data.frame(time = numeric(0), metric_val = numeric(0))
  }
  # ------------------------------------------------

  # 4. Build the ggplot
  p <- ggplot(plot_df, aes(x = time, y = metric_val)) +
    geom_hex(binwidth = my_binwidth) +
    scale_fill_continuous(type = "viridis") +

    # --- NEW: Replace geom_smooth with geom_line using our custom spline ---
    geom_line(data = spline_df, aes(x = time, y = metric_val), color = "red", linewidth = 1.2) +

    coord_cartesian(ylim = c(lower_limit, upper_limit)) +
    labs(
      title = title,
      x = "Time (Years)",
      y = sprintf("%s (%s)", metric_display_name, metric_unit),
      fill = "Density"
    ) +
    theme_minimal() +
    theme(plot.title = element_text(hjust = 0.5))

  return(p)
}
