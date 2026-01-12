# ------------------------------------------------------------------------------

plot_project <- function(model, id, data, metric, metric_unit, metric_display_name) {
  projectData <- data[data$id == id, ]
  relative_introduction_time <- projectData[["relative_introduction_time"]][1]

  randomEffectsFromProject <- ranef(model)[id, ]
  fixedEffects <- fixef(model)

  intercept          <- fixedEffects["(Intercept)"]        + randomEffectsFromProject[["(Intercept)"]]        + relative_introduction_time * fixedEffects["relative_introduction_time"]
  base_slope         <- fixedEffects["base_slope"]         + randomEffectsFromProject[["base_slope"]]         + relative_introduction_time * fixedEffects["relative_introduction_time:base_slope"]
  intervention_jump  <- fixedEffects["intervention_jump"]  + randomEffectsFromProject[["intervention_jump"]]  + relative_introduction_time * fixedEffects["relative_introduction_time:intervention_jump"]
  intervention_slope <- fixedEffects["intervention_slope"] + randomEffectsFromProject[["intervention_slope"]] + relative_introduction_time * fixedEffects["relative_introduction_time:intervention_slope"]

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


  # predicted graph
  plot(
    predictedData$x, predictedData$y,
    type = "l",
    xlab = "Time (Years)",
    ylab = sprintf("%s (%s)", metric_display_name, metric_unit),
    ylim = c(lower_limit, upper_limit),
    lwd = 2,
    col = "red",
    main = paste0("Project: ", id)
  )

  # scatter
  points(
    projectData$time, projectData$metric,
    col = "lightblue",
    pch = 4
  )

  # intervention_jump line
  abline(v = 0, lty = 2, col = "darkgrey")


  # legend
  legend("topright",
         legend = c("Fitted", "Weekly Data", "Linter Intro."),
         col = c("red", "lightblue", "darkgrey"),
         lty = c(1, NA, 2),
         pch = c(NA, 4, NA))

  # text
  plot_text <- paste0(
    "Project Age at Intro. = ", round(relative_introduction_time, 3), "\n",
    "Intercept = ",             round(intercept, 3),                  "\n",
    "Base Slope = ",            round(base_slope, 3),                 "\n",
    "Intervention Jump = ",     round(intervention_jump, 3),          "\n",
    "Intervention Slope = ",    round(intervention_slope, 3),         "\n"
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
