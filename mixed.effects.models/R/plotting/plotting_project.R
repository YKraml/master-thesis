# ------------------------------------------------------------------------------

plot_project_1 <- function(model, id, data) {
  random_effects <- ranef(model)
  randomEffectsFromProject = random_effects[id,]
  fixedEffects <- fixef(model)
  randomEffectsFromProject <- fixedEffects + randomEffectsFromProject

  # compute predicted values
  x <- seq(-2, 2, by = 0.01)
  predictedData <- data.frame(
    x = x,
    y = ifelse(
      x < 0,
      randomEffectsFromProject$`(Intercept)` + randomEffectsFromProject$t_pre * x,
      randomEffectsFromProject$`(Intercept)` + randomEffectsFromProject$intervention + randomEffectsFromProject$t_post * x
    )
  )

  # scatter plot data
  projectData <- data[data$id == id, ]
  projectData <- projectData[c(metric, "t_pre", "t_post")]
  projectData <- data.frame(
    time = ifelse(
      projectData$t_pre != 0,
      projectData$t_pre,
      projectData$t_post
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
  seconds_timestamp <- l[l$intervention == 1, ][1, ]$time
  date_time_object <- as.POSIXct(seconds_timestamp, origin = "1970-01-01", tz = "GMT")
  date <- format(date_time_object, format = "%d.%m.%Y")


  # predicted graph
  plot(
    predictedData$x, predictedData$y,
    type = "l",
    xlab = "Time in years",
    ylab = paste("Metric:", metric),
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

  # intervention line
  abline(v = 0, lty = 2, col = "darkgrey")


  # legend
  legend("topright",
         legend = c("Fitted", "Actual data", paste0("Introduction: ", date)),
         col = c("red", "lightblue", "darkgrey"),
         lty = c(1, NA, 2),
         pch = c(NA, 4, NA))
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_project_2 <- function(model, id, data) {

  random_effects <- ranef(model)
  fixedEffects <- fixef(model)
  randomEffectsFromProject <- fixedEffects + random_effects[id,]

  # compute predicted values
  x <- seq(0, 2, by = 0.01)
  predictedData <- data.frame(
    x = x,
    y = randomEffectsFromProject$`(Intercept)` + randomEffectsFromProject$t_post * x
  )

  # scatter plot data
  projectData <- data[data$id == id, ]
  projectData <- projectData[c(metric, "t_post")]


  # limits
  ymin_all <- min(c(predictedData$y, projectData[[metric]]))
  ymax_all <- max(c(predictedData$y, projectData[[metric]]))
  ydiff_all <- ymax_all - ymin_all
  upper_limit <- ymax_all + ydiff_all / 2
  lower_limit <- ymin_all - ydiff_all / 2

  # get introduction date
  l <- data[data$id == id, ]
  seconds_timestamp <- l[l$intervention == 1, ][1, ]$time
  date_time_object <- as.POSIXct(seconds_timestamp, origin = "1970-01-01", tz = "GMT")
  date <- format(date_time_object, format = "%d.%m.%Y")


  # predicted graph
  plot(
    predictedData$x, predictedData$y,
    type = "l",
    xlab = "Time in years",
    ylab = paste("Metric:", metric),
    ylim = c(lower_limit, upper_limit),
    lwd = 2,
    col = "red",
    main = paste0("Project: ", id)
  )

  # scatter
  points(
    projectData$t_post, projectData[[metric]],
    col = "lightblue",
    pch = 4
  )

  # intervention line
  abline(v = 0, lty = 2, col = "darkgrey")


  # legend
  legend("topright",
         legend = c("Fitted", "Actual data", paste0("Introduction: ", date)),
         col = c("red", "lightblue", "darkgrey"),
         lty = c(1, NA, 2),
         pch = c(NA, 4, NA))
}



