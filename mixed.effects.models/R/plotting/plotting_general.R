

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
    main = paste0("Histogram at Linter Introduction of: ", metric),
    xlim = c(0, mean + 3 * sigma)
  )
}

# ------------------------------------------------------------------------------



