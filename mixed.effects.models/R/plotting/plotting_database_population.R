# ------------------------------------------------------------------------------

plot_merged_star_polygons <- function(java_data, python_data, cpp_data){

  # Save the current graphical parameters and adjust margins to remove top title padding
  old_par <- par(no.readonly = TRUE)
  par(mar = c(5, 4, 1, 2) + 0.1)

  java_stars   <- log10(java_data$numberOfStars + 1)
  python_stars <- log10(python_data$numberOfStars + 1)
  cpp_stars    <- log10(cpp_data$numberOfStars + 1)

  # 1. Calculate Densities
  d_java   <- density(java_stars)
  d_python <- density(python_stars)
  d_cpp    <- density(cpp_stars)

  colors_fill <- c(
    java   = rgb(123, 104, 238, maxColorValue = 255, alpha = 100),
    python = rgb(248, 118, 109, maxColorValue = 255, alpha = 100),
    cpp    = rgb(0, 191, 196, maxColorValue = 255, alpha = 100)
  )

  colors_border <- c(
    java   = "#7B68EE",
    python = "#F8766D",
    cpp    = "#00BFC4"
  )

  # 2. Initialize Plot with fixed limits and no title
  plot(NA,
       main = "",
       xlab = "Number of Stars (Log Scale)",
       ylab = "Density",
       xlim = c(log10(100), log10(100000)), # Limits x-axis from 100 to 100,000
       ylim = c(0, 2),                      # Limits y-axis from 0 to 2
       bty  = "l",
       xaxt = "n")

  # 3. Create Custom X-Axis (Translating log10 back to readable numbers)
  # Set exactly between log10(100)=2 and log10(100000)=5
  major_ticks <- seq(2, 5, by = 1)
  tick_labels <- format(10^major_ticks, scientific = FALSE, big.mark = ",")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  # 4. Draw Polygons
  polygon(d_java,   col = colors_fill["java"],   border = colors_border["java"])
  polygon(d_python, col = colors_fill["python"], border = colors_border["python"])
  polygon(d_cpp,    col = colors_fill["cpp"],    border = colors_border["cpp"])

  # 5. Add Legend
  legend("topright",
         legend = c(
           "Java",
           "Python",
           "C++"
         ),
         fill = colors_fill,
         border = colors_border)

  # Restore original plotting parameters
  par(old_par)
}

# ------------------------------------------------------------------------------



# ------------------------------------------------------------------------------

plot_star_hist <- function(data, language_name, color_hex) {
  old_par <- par(no.readonly = TRUE)
  par(mar = c(5, 4, 1, 2) + 0.1)

  log_stars <- log10(data$numberOfStars + 1)

  hist(
    log_stars[log_stars < log10(100000)],
    breaks = 30,
    freq = TRUE,
    main = "",
    xlab = "Number of Stars (Log Scale)",
    ylab = "Frequency",
    col = color_hex,
    border = "black",
    xaxt = "n",
    xlim = c(log10(100), log10(100000)), # Limits x-axis from 100 to 100,000 (in log10 scale)
    ylim = c(0, 4000)                    # Limits y-axis from 0 to 4,000
  )

  # Generate ticks based on the new fixed limits (log10(100) = 2, log10(100000) = 5)
  at_ticks <- seq(2, 5, by = 1)
  labels   <- format(10^at_ticks, scientific = FALSE, big.mark = ",")
  axis(side = 1, at = at_ticks, labels = labels)

  legend("topright",
         legend = c(paste0(language_name, " (N = ", nrow(data), ")")),
         fill = c(color_hex),
         border = c(color_hex))

  # Restore the original parameters so it doesn't affect subsequent plots
  par(old_par)
}

# ------------------------------------------------------------------------------
