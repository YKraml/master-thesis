# ------------------------------------------------------------------------------

plot_project <- function(model, id, data, metric, metric_unit, metric_display_name, transition_n_days) {
  gap_end <- transition_n_days / 365

  projectData <- data[data$id == id, ]
  relative_introduction_time <- projectData[["relative_introduction_time"]][1]

  randomEffectsFromProject <- ranef(model)[id, ]

  parameters <- get_parameters(model, relative_introduction_time)

  intercept          <- randomEffectsFromProject[["(Intercept)"]]        + parameters$Value[1]
  base_slope         <- randomEffectsFromProject[["base_slope"]]         + parameters$Value[2]
  intervention_jump  <- randomEffectsFromProject[["intervention_jump"]]  + parameters$Value[3]
  intervention_slope <- randomEffectsFromProject[["intervention_slope"]] + parameters$Value[4]

  # compute predicted values
  x <- seq(max(-2, -relative_introduction_time), 2, by = 0.01)
  predictedData <- data.frame(
    x = x,
    y = ifelse(
      x < 0,
      intercept + base_slope * x,
      intercept + base_slope * x + intervention_jump + intervention_slope * x
    )
  )
  predictedData <- subset(predictedData, x < 0 | x > transition_n_days / 365)

  # scatter plot data
  projectData <- data[data$id == id, ]
  projectData <- projectData[c(metric, "base_slope", "intervention_slope")]
  projectData <- data.frame(
    time = ifelse(
      projectData$base_slope != 0,
      projectData$base_slope,
      projectData$intervention_slope
    ),
    metric = projectData[[metric]]
  )

  # limits
  ymin_all <- min(c(predictedData$y, projectData$metric))
  ymax_all <- max(c(predictedData$y, projectData$metric))
  ydiff_all <- ymax_all - ymin_all
  upper_limit <- ymax_all + ydiff_all / 2
  lower_limit <- ymin_all - ydiff_all / 2

  # get introduction date
  l <- data[data$id == id, ]
  seconds_timestamp <- l[l$intervention_jump == 1, ][1, ]$time
  date_time_object <- as.POSIXct(seconds_timestamp, origin = "1970-01-01", tz = "GMT")
  date <- format(date_time_object, format = "%d.%m.%Y")

  idx_pre <- which(predictedData$x <= 0)
  idx_post <- which(predictedData$x >= gap_end)


  # predicted graph
  plot(
    predictedData$x, predictedData$y,
    type = "n",
    xlab = "Time (Years)",
    ylab = sprintf("%s (%s)", metric_display_name, metric_unit),
    ylim = c(lower_limit, upper_limit),
    main = paste0("Project: ", id),
    cex.main = 1.1, cex.lab = 1.1, las = 1
  )
  lines(predictedData$x[idx_pre], predictedData$y[idx_pre], col = "red", lwd = 2.5)
  lines(predictedData$x[idx_post], predictedData$y[idx_post], col = "red", lwd = 2.5)


  # scatter
  points(
    projectData$time, projectData$metric,
    col = "lightblue",
    pch = 4
  )

  # intervention_jump line
  abline(v = 0, lty = 2, col = "darkgrey")

  # legend
  legend(
    "topright",
    legend = c("Fitted Line", "Weekly Data", "Transition Start & End", "Transition"),
    col = c("red", "lightblue", "darkgrey", rgb(0.9, 0.9, 0.9, alpha = 0.4)),
    lty = c(1, NA, 2, 1),
    pch = c(NA, 4, NA, NA),
    lwd = c(1, 1, 1, 10)
  )


  # text
  plot_text <- paste0(
    "Project Age at Intro. = ", round(relative_introduction_time, 3), "\n",
    "Intercept = ",             round(intercept, 3),                  "\n",
    "Base Slope = ",            round(base_slope, 3),                 "\n",
    "Intervention Jump = ",     round(intervention_jump, 3),          "\n",
    "Intervention Slope = ",    round(intervention_slope, 3),         "\n"
  )

  # Intervention Lines
  abline(v = 0, lty = 2, col = "darkgrey", lwd = 1.5)
  abline(v = gap_end , lty = 2, col = "darkgrey", lwd = 1.5)
  rect(
    xleft = 0,
    ybottom = par("usr")[3], # extend to bottom of plot area
    xright = gap_end,
    ytop = par("usr")[4],    # extend to top of plot area
    col = rgb(0.9, 0.9, 0.9, alpha = 0.4), # Very light grey
    border = NA
  )


  txt_x <- max(-2, -relative_introduction_time)
  txt_y <- upper_limit
  txt_w <- strwidth(plot_text, cex = 0.8)
  txt_h <- strheight(plot_text, cex = 0.8)
  rect(
    xleft   = txt_x,
    ybottom = txt_y - txt_h,
    xright  = txt_x + txt_w,
    ytop    = txt_y,
    col     = "white",
    border  = NA
  )
  text(x = txt_x, y = txt_y, labels = plot_text, adj = c(0, 1), cex = 0.8)
}

# ------------------------------------------------------------------------------

plot_project_scatter <- function(id, data, metric, metric_unit, metric_display_name, color1, color2){

  # 1. Filter and prepare data
  projectData <- data[data$id == id, ]

  # Create a clean plotting frame
  plot_df <- data.frame(
    time = ifelse(
      projectData$base_slope != 0,
      projectData$base_slope,
      projectData$slope_change
    ),
    metric_val = projectData[[metric]]
  )

  # Remove NAs to prevent the spline function from crashing
  plot_df <- na.omit(plot_df)


  ymin_all <- min(plot_df$metric_val)
  ymax_all <- max(plot_df$metric_val)
  ydiff_all <- ymax_all - ymin_all
  upper_limit <- ymax_all + ydiff_all / 2
  lower_limit <- ymin_all - ydiff_all / 2

  # 2. Initialize the plot
  plot(
    plot_df$time,
    plot_df$metric_val,
    xlab = "Time (Years)",
    ylab = sprintf("%s (%s)", metric_display_name, metric_unit),
    ylim = c(lower_limit, upper_limit),
    main = id,
    col = color1,
    pch = 4
  )

  # 3. Add Smoothing Spline
  # We check if we have enough points to fit a spline
  if(nrow(plot_df) >= 4) {
    spline_fit <- smooth.spline(plot_df$time, plot_df$metric_val, spar = 0.7)
    lines(spline_fit, col = color2, lwd = 2)
  }

  # 4. Update legend to include the spline
  legend(
    "topright",
    legend = c("Weekly Data", "Smoothing Spline"),
    col = c(color1, color2),
    pch = c(4, NA),
    lty = c(NA, 1),
    lwd = c(1, 2),
  )
}

# ------------------------------------------------------------------------------
