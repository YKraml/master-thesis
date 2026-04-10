# ------------------------------------------------------------------------------

plot_model_sensitivity <- function(model, interaction, color) {
  time_values <- seq(0.1, 10, length.out = 100)

  param_list <- lapply(time_values, function(t) {
    params <- get_parameters(model, t, interaction)
    params$time <- t
    return(params)
  })


  plot_data <- do.call(rbind, param_list)
  unique_params <- unique(plot_data$Parameter)

  par(mfrow = c(2, 2))


  sub_data <- plot_data[plot_data$Parameter == unique_params[1], ]
  plot_sensitivity("Intercept", sub_data$time, sub_data$Value, color)


  sub_data <- plot_data[plot_data$Parameter == unique_params[2], ]
  plot_sensitivity("Base Slope", sub_data$time, sub_data$Value, color)


  sub_data <- plot_data[plot_data$Parameter == unique_params[3], ]
  plot_sensitivity("Level Change", sub_data$time, sub_data$Value, color)


  sub_data <- plot_data[plot_data$Parameter == unique_params[4], ]
  plot_sensitivity("Slope Change", sub_data$time, sub_data$Value, color)


  par(mfrow = c(1,1))
}

# ------------------------------------------------------------------------------

plot_sensitivity <- function(main, x, y , color){

  plot(x,
       y,
       type = "l",
       lwd = 2,
       xlab = "Project Age at Linter Introduction (Years)",
       ylab = "Parameter Estimate",
       main = main,
       col = color)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

}
