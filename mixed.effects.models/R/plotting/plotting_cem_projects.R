# ------------------------------------------------------------------------------

plot_star_count <- function(control_data, treated_data) {
  control_stars <- log10(control_data$numberOfStars + 1)
  treated_stars <- log10(treated_data$numberOfStars + 1)

  d_control <- density(control_stars)
  d_treated <- density(treated_stars)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Star Count of Projects",
       xlab = "Number of Stars (Log Scale)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l",
       xaxt = "n")

  major_ticks <- seq(floor(x_lim[1]), ceiling(x_lim[2]), by = 1)
  tick_labels <- formatC(10^major_ticks, format = "f", digits = 0, big.mark = "")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
         )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_creation_year <- function(control_data, treated_data) {
  control_years <- control_data$creationYear
  treated_years <- treated_data$creationYear

  d_control <- density(control_years)
  d_treated <- density(treated_years)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Creation Years of Projects",
       xlab = "Year",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l")

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
         )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_commit_count <- function(control_data, treated_data) {
  control_commits <- log10(control_data$numberOfCommits + 1)
  treated_commits <- log10(treated_data$numberOfCommits + 1)

  d_control <- density(control_commits)
  d_treated <- density(treated_commits)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Number of Commits of Projects",
       xlab = "Number of Commits (Log Scale)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l",
       xaxt = "n")

  major_ticks <- seq(floor(x_lim[1]), ceiling(x_lim[2]), by = 1)
  tick_labels <- formatC(10^major_ticks, format = "f", digits = 0, big.mark = "")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
         )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_linter_age <- function(control_data, treated_data) {
  d_control <- density(control_data$time_diff, na.rm = TRUE)
  d_treated <- density(treated_data$time_diff, na.rm = TRUE)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Project Age at Linter Introduction",
       xlab = "Project Age (Years)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l")

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
         )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_linter_intro_year <- function(control_data, treated_data) {
  control_years <- control_data$linter_intro_year
  treated_years <- treated_data$linter_intro_year

  d_control <- density(control_years, na.rm = TRUE)
  d_treated <- density(treated_years, na.rm = TRUE)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Linter Introduction Years",
       xlab = "Year",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l")

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
         )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_window_commits <- function(control_data, treated_data) {
  control_commits <- log10(control_data$numberOfCommitsInTwoYearWindow + 1)
  treated_commits <- log10(treated_data$numberOfCommitsInTwoYearWindow + 1)

  d_control <- density(control_commits)
  d_treated <- density(treated_commits)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Number of Commits of Projects in Analysis Window",
       xlab = "Number of Commits (Log Scale)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l",
       xaxt = "n")

  major_ticks <- seq(floor(x_lim[1]), ceiling(x_lim[2]), by = 1)
  tick_labels <- formatC(10^major_ticks, format = "f", digits = 0, big.mark = "")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
        )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_linter_commit_indicies <- function(control_data, treated_data) {
  control_commits <- log10(control_data$linterCommitIndex + 1)
  treated_commits <- log10(treated_data$linterCommitIndex + 1)

  d_control <- density(control_commits)
  d_treated <- density(treated_commits)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Linter Commit Index",
       xlab = "Linter Commit Index (Log Scale)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l",
       xaxt = "n")

  major_ticks <- seq(floor(x_lim[1]), ceiling(x_lim[2]), by = 1)
  tick_labels <- formatC(10^major_ticks, format = "f", digits = 0, big.mark = "")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
  )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_sloc <- function(control_data, treated_data) {
  control_sloc <- log10(control_data$sloc + 1)
  treated_sloc <- log10(treated_data$sloc + 1)

  d_control <- density(control_sloc)
  d_treated <- density(treated_sloc)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Source Lines of Code",
       xlab = "SLOC (Log Scale)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l",
       xaxt = "n")

  major_ticks <- seq(floor(x_lim[1]), ceiling(x_lim[2]), by = 1)
  tick_labels <- formatC(10^major_ticks, format = "f", digits = 0, big.mark = "")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
  )
}

# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------

plot_number_of_contributors <- function(control_data, treated_data) {
  control_contributors <- log10(control_data$numberOfContributors + 1)
  treated_contributors <- log10(treated_data$numberOfContributors + 1)

  d_control <- density(control_contributors)
  d_treated <- density(treated_contributors)

  gg_red  <- rgb(248, 118, 109, maxColorValue = 255, alpha = 128)
  gg_blue <- rgb(0, 191, 196, maxColorValue = 255, alpha = 128)

  border_red  <- rgb(248, 118, 109, maxColorValue = 255)
  border_blue <- rgb(0, 191, 196, maxColorValue = 255)

  x_lim <- range(c(d_control$x, d_treated$x))
  y_lim <- range(c(d_control$y, d_treated$y))

  plot(d_control,
       main = "Number of Contributors",
       xlab = "Number of Contributors (Log Scale)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       type = "n",
       bty = "l",
       xaxt = "n")

  major_ticks <- seq(floor(x_lim[1]), ceiling(x_lim[2]), by = 1)
  tick_labels <- formatC(10^major_ticks, format = "f", digits = 0, big.mark = "")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  polygon(d_control, col = gg_red, border = border_red)
  polygon(d_treated, col = gg_blue, border = border_blue)

  legend("topright",
         legend = c("Control", "Treated"),
         fill = c(gg_red, gg_blue),
         border = c(border_red, border_blue)
  )
}
