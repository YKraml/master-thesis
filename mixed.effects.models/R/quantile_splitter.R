# ------------------------------------------------------------------------------

split_data_into_quantiles <- function(data, no_quartiles, metric){
  data_t0 <- subset(data, t == 0)

  # Calculate quantiles based on no_quartiles
  probs <- seq(0, 1, length.out = no_quartiles + 1)[2:no_quartiles]
  percentiles <- quantile(data_t0[[metric]], probs = probs)

  # Create quartile labels dynamically
  quartile_labels <- paste0("Q", 1:no_quartiles)

  data_t0$quartile <- cut(
    data_t0[[metric]],
    breaks = c(-Inf, percentiles, Inf),
    labels = quartile_labels
  )

  # Create a list to store data subsets for each quartile
  splitted_data <- lapply(quartile_labels, function(q) {
    ids <- data_t0$id[data_t0$quartile == q]
    subset(data, id %in% ids)
  })

  return(splitted_data)
}

# ------------------------------------------------------------------------------
