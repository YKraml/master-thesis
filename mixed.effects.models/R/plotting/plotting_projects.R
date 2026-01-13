plot_number_of_stars_hist <- function(data){
  hist(log10(data$numberOfStars + 1),
       xaxt = "n",
       col = "lightblue",
       border = "black",
       xlab = "Number of Stars",
       ylab = "Number of Projects",
       main = "Star Count of Projects")
  log_ticks <- 0:10
  axis(1, at = log_ticks, labels = 10^log_ticks)
}

plot_age_at_intro_hist <- function(data){
  intro_time <- as.numeric(data$introductionTime)
  first_time <- as.numeric(data$firstCommitTime)
  years <- (intro_time - first_time) / 60 / 60 / 24 / 365
  years <- years[!is.na(years)]

  breaks_seq <- seq(0, 20, by = 1)
  hist(years[years > 0 & years < 20],
       breaks = breaks_seq,
       col = "lightblue",
       border = "black",
       xlab = "Age at Intro (Years)",
       ylab = "Number of Projects",
       main = "Project Age at Linter Introduction")
}

plot_project_creation_year_hist <- function(data){
  dates <- as.POSIXct(as.numeric(data$firstCommitTime), origin = "1970-01-01", tz = "UTC")
  years <- as.numeric(format(dates, "%Y"))
  hist(years[years > 1990],
       col = "lightblue",
       border = "black",
       breaks = seq(1995, 2025, 1),
       xlab = "Year",
       ylab = "Number of Projects",
       main = "Creation Years of Projects")
}
