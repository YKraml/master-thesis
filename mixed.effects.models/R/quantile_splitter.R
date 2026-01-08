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

# ------------------------------------------------------------------------------

split_data_by_relative_introduction <- function(data) {


  df1 <- data[
    data$relative_introduction_time < 1,
    ]

  df2 <- data[
    data$relative_introduction_time >= 1 & data$relative_introduction_time < 2,
    ]

  df3 <- data[
    data$relative_introduction_time >= 2 & data$relative_introduction_time < 4,
    ]

  df4 <- data[
    data$relative_introduction_time >= 4 & data$relative_introduction_time < 6,
  ]

  df5 <- data[
    data$relative_introduction_time >= 6 & data$relative_introduction_time < 8,
  ]

  df6 <- data[
    data$relative_introduction_time >= 8,
  ]

  # Combine into a list for easy access
  splitted_data <- list(
    less_then_one_year= df1,
    between_one_and_two_years= df2,
    between_two_and_four_years= df3,
    between_four_and_six_years= df4,
    between_six_and_eight_years= df5,
    more_then_eight_years= df6
  )

  return(splitted_data)
}

# ------------------------------------------------------------------------------
