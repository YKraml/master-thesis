# ------------------------------------------------------------------------------

plot_merged_star_polygons <- function(java_data, python_data, cpp_data){

  java_stars   <- log10(java_data$numberOfStars + 1)
  python_stars <- log10(python_data$numberOfStars + 1)
  cpp_stars    <- log10(cpp_data$numberOfStars + 1)


  # 1. Calculate Densities
  d_java   <- density(java_stars)
  d_python <- density(python_stars)
  d_cpp    <- density(cpp_stars)

  # 2. Define Colors (Using semi-transparent RGBA for overlaps)
  colors_fill <- c(
    java   = rgb(248, 118, 109, maxColorValue = 255, alpha = 100), # Red-ish
    python = rgb(0, 191, 196, maxColorValue = 255, alpha = 100),   # Teal-ish
    cpp    = rgb(123, 104, 238, maxColorValue = 255, alpha = 100)  # SlateBlue
  )

  colors_border <- c(
    java   = rgb(248, 118, 109, maxColorValue = 255),
    python = rgb(0, 191, 196, maxColorValue = 255),
    cpp    = rgb(123, 104, 238, maxColorValue = 255)
  )

  # 3. Calculate Global Ranges for the Plot
  x_lim <- range(c(d_java$x, d_python$x, d_cpp$x))
  y_lim <- range(0, max(c(d_java$y, d_python$y, d_cpp$y)))

  # 4. Initialize Plot
  plot(NA,
       main = "Star Count Distribution by Language",
       xlab = "Number of Stars (Log Scale)",
       ylab = "Density",
       xlim = x_lim,
       ylim = y_lim,
       bty  = "l",
       xaxt = "n")

  # 5. Create Custom X-Axis (Translating log10 back to readable numbers)
  major_ticks <- seq(floor(x_lim[1]), ceiling(x_lim[2]), by = 1)
  tick_labels <- format(10^major_ticks, scientific = FALSE, big.mark = ",")
  axis(side = 1, at = major_ticks, labels = tick_labels)

  grid(nx = NULL, ny = NULL, col = "gray90", lty = "dotted")

  # 6. Draw Polygons
  polygon(d_java,   col = colors_fill["java"],   border = colors_border["java"])
  polygon(d_python, col = colors_fill["python"], border = colors_border["python"])
  polygon(d_cpp,    col = colors_fill["cpp"],    border = colors_border["cpp"])

  # 7. Add Legend
  legend("topright",
         legend = c(
           paste0("Java (N = ", nrow(java_data), ")"),
           paste0("Python (N = ", nrow(python_data), ")"),
           paste0("C++ (N = ", nrow(cpp_data), ")")
         ),
         fill = colors_fill,
         border = colors_border,)

}

# ------------------------------------------------------------------------------



# ------------------------------------------------------------------------------

plot_star_hist <- function(data, language_name, color_hex) {
  log_stars <- log10(data$numberOfStars + 1)

  hist(
    log_stars,
    breaks = 30,
    freq = TRUE,
    main = paste("Distribution of Stars:", language_name),
    xlab = "Number of Stars (Log Scale)",
    ylab = "Frequency",
    col = color_hex,
    border = "white", # White borders look cleaner with solid colors
    xaxt = "n"        # Disable default axis to add custom labels
  )

  at_ticks <- seq(floor(min(log_stars)), ceiling(max(log_stars)), by = 1)
  labels   <- format(10^at_ticks, scientific = FALSE, big.mark = ",")
  axis(side = 1, at = at_ticks, labels = labels)

  legend("topright",
         legend = c(paste0(language_name, " (N = ", nrow(data), ")")),
         fill = c(color_hex),
         border = c(color_hex),)

}

# ------------------------------------------------------------------------------
