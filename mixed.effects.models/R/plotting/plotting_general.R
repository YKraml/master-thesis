

# ------------------------------------------------------------------------------

plot_raw_data_hist <- function(data, metric){
  sigma <- sd(data)
  mean <- mean(data)
  hist(
    data[data <= mean + 3 * sigma],
    breaks = 30,
    freq = TRUE,
    xlab = metric,
    col = "lightblue",
    border = "black",
    main = paste0("Histogram of: ", metric),
    xlim = c(0, mean + 3 * sigma)
  )
}

# ------------------------------------------------------------------------------



