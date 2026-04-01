library(nlme)
library(performance)
library(tidyverse)
library(ggplot2)
library(effects)
library(cowplot)
library(lme4)
library(sjPlot)
library(sjmisc)
library(sjstats)
library(lattice)
library(lmerTest)
library(DHARMa)
library(dplyr)
library(glmmTMB)
library(AICcmodavg)
library(tibble)
library(patchwork)
library(RColorBrewer)
library(ggeffects)
library(multcomp)
library(splines)
library(marginaleffects)
library(glmertree)
library(mgcv)
library(robustlmm)
library(hexbin)

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/plotting/plotting_lagged_transition_time.R")
source("R/plotting/plotting_lagged_day_zero.R")
source("R/plotting/plotting_cem_projects.R")
source("R/plotting/plotting_sensitivity.R")
source("R/plotting/plotting_effects_distributions.R")
source("R/plotting/plotting_database_population.R")


source("R/saver.R")
source("R/shared.R")
source("R/lagged_transition_time.R")
source("R/lagged_day_zero.R")

# ------------------------------------------------------------------------------
# Shared
# ------------------------------------------------------------------------------

languages <- list(
  python = list(name = "python", display_name ="Python", color = "#7B68EE", color_secondary = "#D3CCFF"),
  java   = list(name = "java",   display_name ="Java", color = "#F8766D", color_secondary = "#FFD5D1"),
  cpp    = list(name = "cpp",    display_name ="C++",    color = "#00BFC4", color_secondary = "#CCF2F3")
)

metrics <- list(
  # All findings
  findingsDensity          = list(name = "findingsDensity",          display_name = "Findings Density",          unit = "Findings per 1 kLOC"),

  # Findings groups
  comprehensibilityDensity = list(name = "comprehensibilityDensity", display_name = "Comprehensibility Density", unit = "Findings per 1 kLOC"),
  correctnessDensity       = list(name = "correctnessDensity",       display_name = "Correctness Density",       unit = "Findings per 1 kLOC"),
  documentationDensity     = list(name = "documentationDensity",     display_name = "Documentation Density",     unit = "Findings per 1 kLOC"),
  efficiencyDensity        = list(name = "efficiencyDensity",        display_name = "Efficiency Density",        unit = "Findings per 1 kLOC"),
  errorHandlingDensity     = list(name = "errorHandlingDensity",     display_name = "Error Handling Density",    unit = "Findings per 1 kLOC"),
  redundancyDensity        = list(name = "redundancyDensity",        display_name = "Redundancy Density",        unit = "Findings per 1 kLOC"),
  structureDensity         = list(name = "structureDensity",         display_name = "Structure Density",         unit = "Findings per 1 kLOC"),
  securityDensity          = list(name = "securityDensity",          display_name = "Security Density",          unit = "Findings per 1 kLOC"),

  # Structure metrics
  longFiles                = list(name = "longFiles",                display_name = "Long Files",                unit = "Findings"),
  longMethods              = list(name = "longMethods",              display_name = "Long Method",               unit = "Findings"),
  deepNesting              = list(name = "deepNesting",              display_name = "Deep Nesting",              unit = "Findings"),
  longFilesDensity         = list(name = "longFilesDensity",         display_name = "Long Files Density",        unit = "Findings per 1 kLOC"),
  longMethodDensity        = list(name = "longMethodDensity",        display_name = "Long Method Density",       unit = "Findings per 1 kLOC"),
  deepNestingDensity       = list(name = "deepNestingDensity",       display_name = "Deep Nesting Density",      unit = "Findings per 1 kLOC"),
  cloneCoverage            = list(name = "cloneCoverage",            display_name = "Clone Coverage",            unit = "%"),
  averageFileLoc           = list(name = "averageFileLoc",           display_name = "Average File Size",         unit = "LOC per File"),

  # Git metrics
  numberOfForks            = list(name = "numberOfForks",           display_name = "Number of Forks",            unit = "Forks"),
  numberOfIssues           = list(name = "numberOfIssues",          display_name = "Number of Issues",           unit = "Issues"),
  numberOfContributors     = list(name = "numberOfContributors",    display_name = "Number of Contributors",     unit = "Contributors")
  )








# ------------------------------------------------------------------------------
# Population Selection
# ------------------------------------------------------------------------------
source("R/plotting/plotting_database_population.R")

java_data   <- read.csv("data/python/database_population/data.csv")
python_data <- read.csv("data/java/database_population/data.csv")
cpp_data    <- read.csv("data/cpp/database_population/data.csv")

out_dir <- file.path("plots", "database_population")
dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

save_png(
  expression = plot_merged_star_polygons(java_data, python_data, cpp_data),
  file = file.path(out_dir, "merged_stars.png"),
  width = 10,
  height = 8
)

save_png(
  expression = plot_star_hist(java_data, "Java", languages$java$color),
  file = file.path(out_dir, "java_stars.png"),
  width = 10,
  height = 8
)

save_png(
  expression = plot_star_hist(python_data, "Python", languages$python$color),
  file = file.path(out_dir, "python_stars.png"),
  width = 10,
  height = 8
)

save_png(
  expression = plot_star_hist(cpp_data, "C++", languages$cpp$color),
  file = file.path(out_dir, "cpp_stars.png"),
  width = 10,
  height = 8
)


# ------------------------------------------------------------------------------
# CEM-Projects
# ------------------------------------------------------------------------------

for(language in languages){
  language_name <- language$name
  language_display_name <- language$display_name
  message(paste("---", language_name, "---"))

  control_data <- read.csv(file.path("data", language_name, "cem_projects", "control.csv"))
  control_data$time_diff <- (control_data$linterCommitTime - control_data$firstCommitTime) / 60 / 60 / 24 / 365
  control_data$linter_intro_year <- control_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

  treated_data <- read.csv(file.path("data", language_name, "cem_projects", "treated.csv"))
  treated_data$time_diff <- (treated_data$linterCommitTime - treated_data$firstCommitTime) / 60 / 60 / 24 / 365
  treated_data$linter_intro_year <- treated_data$linterCommitTime / 60 / 60 / 24 / 365 + 1970

  potential_control_data <- read.csv(file.path("data", language_name, "cem_projects", "potential_control.csv"))
  potential_treated_data <- read.csv(file.path("data", language_name, "cem_projects", "potential_treated.csv"))


  out_dir <- file.path("plots", language_name, "cem_projects")
  dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

  save_multipanel_png(
    {
      plot_linter_age(control_data, treated_data, language_display_name)
      plot_window_commits(control_data, treated_data, language_display_name)
      plot_linter_intro_year(control_data, treated_data, language_display_name)
      plot_linter_commit_indicies(control_data, treated_data, language_display_name)
    },
    file = file.path(out_dir, paste0(language_name, "_all_merged_non_balanced.png")),
    mfrow = c(2, 2),
    width = 20,
    height = 14
  )


  save_multipanel_png(
    {
      plot_creation_year(potential_control_data, potential_treated_data, language_display_name)
      plot_creation_year(control_data,treated_data, language_display_name)
      plot_star_count(potential_control_data, potential_treated_data, language_display_name)
      plot_star_count(control_data, treated_data, language_display_name)
      plot_commit_count(potential_control_data, potential_treated_data, language_display_name)
      plot_commit_count(control_data,treated_data, language_display_name)
      plot_sloc(potential_control_data, potential_treated_data, language_display_name)
      plot_sloc(control_data, treated_data, language_display_name)
      plot_number_of_contributors(potential_control_data, potential_treated_data, language_display_name)
      plot_number_of_contributors(control_data, treated_data, language_display_name)
    },
    file = file.path(out_dir, paste0(language_name, "_all_merged.png")),
    mfrow = c(5, 2),
    width = 20,
    height = 28
  )


}


for(language in languages) {
  language_name <- language$name
  message(paste("--- Testing:", language_name, "---"))

  # 1. Load and Preprocess Data
  process_data <- function(path) {
    if (!file.exists(path)) return(NULL)
    df <- read.csv(path)
    # Calculate derived columns from your original script
    df$time_diff <- (df$linterCommitTime - df$firstCommitTime) / (60 * 60 * 24 * 365)
    df$linter_intro_year <- (df$linterCommitTime / (60 * 60 * 24 * 365)) + 1970
    return(df)
  }

  control_data <- process_data(file.path("data", language_name, "cem_projects", "control.csv"))
  treated_data <- process_data(file.path("data", language_name, "cem_projects", "treated.csv"))
  pot_control  <- read.csv(file.path("data", language_name, "cem_projects", "potential_control.csv"))
  pot_treated  <- read.csv(file.path("data", language_name, "cem_projects", "potential_treated.csv"))

  # 2. Helper to run test and return formatted string
  run_stat <- function(var_name, data_ctrl, data_trtd, label) {
    if (is.null(data_ctrl) || is.null(data_trtd) || !var_name %in% names(data_ctrl)) return(NULL)

    res <- wilcox.test(data_ctrl[[var_name]], data_trtd[[var_name]])
    p_val_fmt <- format.pval(res$p.value, digits = 3, eps = 0.001)
    sig <- if(res$p.value < 0.05) "*" else ""

    # Format: Label (35 chars), W (12 chars), p-value (10 chars)
    sprintf("%-35s | %12.1f | %10s %s", label, res$statistic, p_val_fmt, sig)
  }

  # 3. Collect all results
  results_lines <- c(
    sprintf("Wilcoxon Rank Sum Test Results: %s", language_name),
    paste(rep("-", 65), collapse = ""),
    sprintf("%-35s | %12s | %10s", "Variable", "W-Statistic", "p-value"),
    paste(rep("-", 65), collapse = ""),
    run_stat("numberOfStars", control_data, treated_data, "Stars"),
    run_stat("creationYear", control_data, treated_data, "Project Creation Year"),
    run_stat("numberOfCommits", control_data, treated_data, "Commit Counts"),
    run_stat("time_diff", control_data, treated_data, "Linter Age (Years)"),
    run_stat("linter_intro_year", control_data, treated_data, "Linter Introduction Year"),
    run_stat("numberOfCommitsInTwoYearWindow", control_data, treated_data, "Commits (Analysis Window)"),
    run_stat("linterCommitIndex", control_data, treated_data, "Linter Commit Index"),
    run_stat("sloc", control_data, treated_data, "SLOC"),
    run_stat("numberOfContributors", control_data, treated_data, "Number of Contributors"),
    paste(rep("-", 65), collapse = ""),
    "POTENTIAL DATASETS:",
    run_stat("numberOfStars", pot_control, pot_treated, "Potential: Stars"),
    run_stat("creationYear", pot_control, pot_treated, "Potential: Creation Year"),
    run_stat("numberOfCommits", pot_control, pot_treated, "Potential: Commit Counts"),
    run_stat("sloc", pot_control, pot_treated, "Potential: SLOC"),
    run_stat("numberOfContributors", pot_control, pot_treated, "Potential: Contributors"),
    paste(rep("-", 65), collapse = "")
  )

  # 4. Save to one TXT file
  out_dir <- file.path("plots", language_name, "cem_projects")
  dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)
  writeLines(as.character(na.omit(results_lines)),
             file.path(out_dir, paste0(language_name, "_wilcox_summary.txt")))
}


# ------------------------------------------------------------------------------
# Exploratory Data Analysis
# ------------------------------------------------------------------------------

for(language in languages) {
  language_name <- language$name
  language_color <- language$color
  language_color_secondary <- language$color_secondary

  message(paste("---", language_name, "---"))

  data_treatment <- get_data(language, "treatment", 0)
  data_control   <- get_data(language, "control", 0)

  data_all <- list(
    treatment = data_treatment,
    control   = data_control
  )



  # Box plots without outliers
  overview_dir <- file.path("plots", language_name, "exploratory_data_analysis", "all_metrics_overview")
  dir.create(overview_dir, showWarnings = FALSE, recursive = TRUE)
  save_png(
    {
      par(mfrow = c(5, 4), oma = c(0, 0, 0, 0), mar = c(2, 3, 3, 1))
      for (metric_data in metrics) {
        metric_name         <- metric_data$name
        metric_display_name <- metric_data$display_name
        metric_unit         <- metric_data$unit

        boxplot(
          data_treatment[[metric_name]],
          main = metric_display_name,
          col = language_color,
          outline = FALSE
        )
      }
    },
    file = file.path(overview_dir, paste0(language_name, ".all_metrics_boxplot.png")),
    width = 16,
    height = 22.6
  )


  #Box plots with outliers
  save_png(
    {
      par(mfrow = c(5, 4), oma = c(0, 0, 0, 0), mar = c(2, 3, 3, 1))
      for (metric_data in metrics) {
        metric_name         <- metric_data$name
        metric_display_name <- metric_data$display_name
        metric_unit         <- metric_data$unit

        boxplot(
          data_treatment[[metric_name]],
          main = metric_display_name,
          col = language_color,
          outline = TRUE
        )
      }
    },
    file = file.path(overview_dir, paste0(language_name, ".all_metrics_boxplot_with_outliers.png")),
    width = 16,
    height = 22.6
  )


  # Hisotgrams
  save_png(
    {
      par(mfrow = c(5, 4), oma = c(0, 0, 0, 0), mar = c(2, 4, 4, 1))

      for (metric_data in metrics) {
        metric_name         <- metric_data$name
        metric_display_name <- metric_data$display_name
        metric_unit         <- metric_data$unit


        current_data <- na.omit(data_treatment[[metric_name]])

        sigma <- sd(current_data)
        mean_val <- mean(current_data)
        upper_limit <- mean_val + 3 * sigma

        if (is.na(sigma) || sigma == 0) {
          upper_limit <- max(current_data, 0) + 1
        }

        hist(
          current_data[current_data <= upper_limit],
          breaks = 30,
          freq = TRUE,
          xlab = "",
          ylab = "",
          col = language_color,
          border = "black",
          main = metric_display_name,
          xlim = c(0, upper_limit)
        )
      }
    },
    file = file.path(overview_dir, paste0(language_name, ".all_metrics_histogram.png")),
    width = 16,
    height = 22.6
  )










  for (metric_data in metrics) {
    metric_name <- metric_data$name
    metric_display_name <- metric_data$display_name
    metric_unit         <- metric_data$unit


    message(metric_name)

    for(group in c("treatment", "control")){
      message(sprintf(" - %s ", group))
      data <- data_all[[group]]


      message("   * Creating folder")
      out_dir <- file.path("plots", language_name, "exploratory_data_analysis", group, metric_name)
      dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)


      message("   * Creating plots")
      prefix <- sprintf("%s.%s.%s.",language_name, group, metric_name)


      # Individual Projects
      save_multipanel_png(
        {

          i <- 0
          project_out_dir <- file.path(out_dir, "projects_scatter")
          dir.create(project_out_dir, showWarnings = FALSE, recursive = TRUE)
          for (id in unique(data[["id"]])) {
            plot_project_scatter(id, data, metric_name, metric_unit, metric_display_name, language_color_secondary, language_color)
            i <- i + 1
            if(i >= 8){
              break
            }
          }


        },
        file = file.path(out_dir, paste0(prefix, "individuals.png")),
        width = 16,
        height = 22.6,
        mfrow = c(4, 2)
      )

      # Hex Plots
      save_png(
        expression = {
          # Generate the two plots
          p1 <- plot_all_projects_hexbin(data,                                       metric_name, metric_unit, metric_display_name, "All Projects")
          p2 <- plot_all_projects_hexbin(data[data$relative_introduction_time > 2,], metric_name, metric_unit, metric_display_name, "Projects Older Than 2 Years")

          # Combine and print them simultaneously
          print(p1 + p2)
        },
        file = file.path(out_dir, paste0(prefix, "scatter_hex.png")),
        width = 22,
        height = 10
      )


    }

  }
}




# ------------------------------------------------------------------------------
# Model comparison - Random Effects
# ------------------------------------------------------------------------------



for(language in languages) {
  language_name <- language$name
  language_color <- language$color
  language_color_secondary <- language$color_secondary
  data <- get_data(language_name, "treatment", 0)

  message(language_name)

  output_dir <- file.path("plots", language_name, "random_effects_comparison")
  dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

  for (metric in metrics) {
    metric_name <- metric$name
    message(metric_name)

    # Skip if file exists
    file <- file.path(output_dir, sprintf("random_effects_comparison.%s.txt", metric_name))
    if(file.exists(file)){
      message("  * Skip")
      next
    }


    # Shared parameters
    fixed_formula <- as.formula(sprintf("%s ~ base_slope + level_change + slope_change", metric_name))
    ctrl <- lmeControl(opt = "optim")


    message("  * 0 Random Effect")
    m_none <- gls(model = fixed_formula, data = data, method = "ML")

    message("  * 1 Random Effect")
    m_int   <- lme(fixed = fixed_formula, random = ~ 1 | id, data = data, method = "ML", control = ctrl)
    m_base  <- lme(fixed = fixed_formula, random = ~ 0 + base_slope | id, data = data, method = "ML", control = ctrl)
    m_jump  <- lme(fixed = fixed_formula, random = ~ 0 + level_change | id, data = data, method = "ML", control = ctrl)
    m_slope <- lme(fixed = fixed_formula, random = ~ 0 + slope_change | id, data = data, method = "ML", control = ctrl)

    message("  * 2 Random Effect")
    m_int_base   <- lme(fixed = fixed_formula, random = ~ 1 + base_slope | id, data = data, method = "ML", control = ctrl)
    m_int_jump   <- lme(fixed = fixed_formula, random = ~ 1 + level_change | id, data = data, method = "ML", control = ctrl)
    m_int_slope  <- lme(fixed = fixed_formula, random = ~ 1 + slope_change | id, data = data, method = "ML", control = ctrl)
    m_base_jump  <- lme(fixed = fixed_formula, random = ~ 0 + base_slope + level_change | id, data = data, method = "ML", control = ctrl)
    m_base_slope <- lme(fixed = fixed_formula, random = ~ 0 + base_slope + slope_change | id, data = data, method = "ML", control = ctrl)
    m_jump_slope <- lme(fixed = fixed_formula, random = ~ 0 + level_change + slope_change | id, data = data, method = "ML", control = ctrl)

    message("  * 3 Random Effect")
    m_int_base_jump   <- lme(fixed = fixed_formula, random = ~ 1 + base_slope + level_change | id, data = data, method = "ML", control = ctrl)
    m_int_base_slope  <- lme(fixed = fixed_formula, random = ~ 1 + base_slope + slope_change | id, data = data, method = "ML", control = ctrl)
    m_int_jump_slope  <- lme(fixed = fixed_formula, random = ~ 1 + level_change + slope_change | id, data = data, method = "ML", control = ctrl)
    m_base_jump_slope <- lme(fixed = fixed_formula, random = ~ 0 + base_slope + level_change + slope_change | id, data = data, method = "ML", control = ctrl)

    message("  * 4 Random Effect")
    m_full <- lme(fixed = fixed_formula, random = ~ 1 + base_slope + level_change + slope_change | id, data = data, method = "ML", control = ctrl)

    message("  * Comparing")
    model_comparison <- anova(
      m_none,
      m_int, m_base, m_jump, m_slope,
      m_int_base, m_int_jump, m_int_slope, m_base_jump, m_base_slope, m_jump_slope,
      m_int_base_jump, m_int_base_slope, m_int_jump_slope, m_base_jump_slope,
      m_full
    )

    message("  * Saving")
    capture.output(
      model_comparison[order(model_comparison$AIC), ],
      file = file
    )
  }
}







# ------------------------------------------------------------------------------
# Interrupted Time Series Loop
# ------------------------------------------------------------------------------


for(language in languages) {
  message(paste("---", language, "---"))

  data_treatment <- get_data(language, "treatment", 90)
  data_control   <- get_data(language, "control", 90)

  data_all <- list(
    treatment = data_treatment,
    control   = data_control
  )

  for (metric_data in metrics) {
    metric_name         <- metric_data$name

    message(metric_name)

    for(group in c("treatment", "control")){
      message(sprintf(" - %s ", group))
      data <- data_all[[group]]


      message("   * Creating folder")
      out_dir <- file.path("plots", language, "interrupted_time_series", group, metric_name)
      dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)


      tryCatch({
        message("   * Creating model")
        formula <- sprintf("%s ~ time_saturated * (1 + base_slope + intervention_jump + intervention_slope)", metric_name)
        model <- lme(
          fixed = as.formula(formula),
          random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
          data = data,
          method = "ML",
          # correlation = corAR1(form = ~ t | id),
          control = lmeControl(opt = "optim")
        )

        message("   * Drawing plots")
        save_everything(
          model = model,
          data = data,
          out_dir = out_dir,
          language = language,
          group = group,
          metric_data = metric_data
        )

      }, error = function(e) {
        warning_msg <- sprintf("FAILURE in Metric: '%s' | Group: '%s'", metric_name, group)
        message(paste0("\n ", warning_msg, "\nError message: ", e$message, "\n"))
      })
    }
  }
}









# ------------------------------------------------------------------------------
# Lagged - Transition Time
# ------------------------------------------------------------------------------

for (metric_key in names(metrics)) {
  metric_data <- metrics[[metric_key]]
  metric_name <- metric_data$name
  message(metric_name)

  # Get data from the different models with the lagged data
  results_list <- get_results(metric_name, 210)

  # Create folder
  out_dir <- file.path("plots", "lagged")
  dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

  # transform data
  results_matrix <- do.call(rbind, results_list)
  x_values <- results_matrix[, 1]
  effect_names <- colnames(results_matrix)[-1]

  # create and safe plot
  save_multipanel_png(
    expression = {
      for (i in 1:length(effect_names)) {
        y_values <- results_matrix[, i + 1]
        effect_name <- effect_names[i]
        plot_lagged(effect_name, x_values, y_values)
      }
    },
    file = file.path(out_dir, sprintf("lagged.%s.png", metric_name)),
    height = 15,
    width = 40,
    mfrow = c(2,4)
  )

}

# ------------------------------------------------------------------------------
# Lagged - Day 0
# ------------------------------------------------------------------------------


output_dir <- file.path("plots", "lagged_day_zero")
dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

data_treatment <- get_data("treatment", 90)
data <- data_treatment


for (metric_name in names(metrics)) {
  metric_unit <- metrics[[metric_name]]

  output_filename <- sprintf("lagged.%s.png", metric_name)
  output_path <- file.path(output_dir, output_filename)

  if (file.exists(output_path)) {
    message(" - Skip: ", metric_name)
    next
  }

  message(" - Generating: ", metric_name)

  # Use metric_name here
  result_table <- compare_lagged_models(data, metric = metric_name, min_lag = -10, max_lag = 20)

  # If plot_lag_comparison accepts a Y-axis label, you can use 'metric_unit' here
  save_png(
    expression = plot_lag_comparison(result_table),
    file = output_path
  )
}





# ------------------------------------------------------------------------------
# Model comparison - Interaction Term
# ------------------------------------------------------------------------------


for(language in languages){
  message(paste("---", language, "---"))
  data <- get_data(language, "treatment", 90)

  output_dir <- file.path("plots", language, "time_interaction_comparison")
  dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

  for (metric in metrics) {
    metric_name <- metric$name
    message(metric_name)

    # Shared parameters
    effects <- "(base_slope + intervention_jump + intervention_slope)"
    random_effects <- ~ 1 + base_slope + intervention_jump + intervention_slope | id
    ctrl <- lmeControl(opt = "optim")

    message("1. Linear Model")
    f_lin <- as.formula(sprintf("%s ~ relative_introduction_time * %s", metric_name, effects))
    m_lin <- lme(fixed = f_lin, random = random_effects, data = data, method = "ML", control = ctrl)

    message("2. Saturation Model")
    f_sat <- as.formula(sprintf("%s ~ time_saturated * %s", metric_name, effects))
    m_sat <- lme(fixed = f_sat, random = random_effects, data = data, method = "ML", control = ctrl)

    message("3. Logarithmic Model")
    f_log <- as.formula(sprintf("%s ~ log_time * %s", metric_name, effects))
    m_log <- lme(fixed = f_log, random = random_effects, data = data, method = "ML", control = ctrl)

    message("4. Polynomial Models (Degrees 1 to 3)")
    f_poly1 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 1) * %s", metric_name, effects))
    m_poly1 <- lme(fixed = f_poly1, random = random_effects, data = data, method = "ML", control = ctrl)

    f_poly2 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 2) * %s", metric_name, effects))
    m_poly2 <- lme(fixed = f_poly2, random = random_effects, data = data, method = "ML", control = ctrl)

    f_poly3 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 3) * %s", metric_name, effects))
    m_poly3 <- lme(fixed = f_poly3, random = random_effects, data = data, method = "ML", control = ctrl)


    message("5. Natural Spline Models (Degrees of Freedom 1 to 3)")
    f_spline1 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 1) * %s", metric_name, effects))
    m_spline1 <- lme(fixed = f_spline1, random = random_effects, data = data, method = "ML", control = ctrl)

    f_spline2 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 2) * %s", metric_name, effects))
    m_spline2 <- lme(fixed = f_spline2, random = random_effects, data = data, method = "ML", control = ctrl)

    f_spline3 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 3) * %s", metric_name, effects))
    m_spline3 <- lme(fixed = f_spline3, random = random_effects, data = data, method = "ML", control = ctrl)


    message("6. Compare models")
    model_comparison <- anova(m_lin, m_sat, m_log, m_poly1, m_poly2, m_poly3, m_spline1, m_spline2, m_spline3)

    # Save results sorted by AIC
    capture.output(
      model_comparison[order(model_comparison$AIC), ],
      file = file.path(output_dir, sprintf("%s.%s.time_interaction_comparison.txt", language, metric_name))
    )
  }

}



# ------------------------------------------------------------------------------
# Treated vs Control Effects Distributions
# ------------------------------------------------------------------------------

transition_n_days <- 90

age_ranges <- list(
  c(0, 0.1),
  c(0.1, 0.5),
  c(0.5, 1),
  c(1, 2),
  c(2, 4),
  c(4, 20)
)

data_treatment_full <- get_data("python", "treatment", transition_n_days)
data_treatment_full$time_saturated <- 1 - exp(-1 * data_treatment_full$relative_introduction_time)

data_control_full <- get_data("python", "control", transition_n_days)
data_control_full$time_saturated <- 1 - exp(-1 * data_control_full$relative_introduction_time)



for (range in age_ranges) {
  min_project_age <- range[1]
  max_project_age <- range[2]

  message(sprintf("Time Span: %s to %s years", min_project_age, max_project_age))

  data_treatment <- data_treatment_full[
    data_treatment_full$relative_introduction_time >= min_project_age &
      data_treatment_full$relative_introduction_time <= max_project_age,
  ]

  data_control <- data_control_full[
    data_control_full$relative_introduction_time >= min_project_age &
      data_control_full$relative_introduction_time <= max_project_age,
  ]

  # Optional: Check if we have enough data to proceed, skip if empty to avoid errors
  if (nrow(data_treatment) == 0 || nrow(data_control) == 0) {
    message("    Skipping: Insufficient data for this time span.")
    next
  }


  for (metric in metrics) {
    metric_name <- metric$name
    message(paste("    Metric:", metric_name))


    message("    * Calculating treated model")
    model_treatment <- tryCatch({
      lme(
        fixed = as.formula(sprintf("%s ~ time_saturated * (1 + base_slope + intervention_jump + intervention_slope)", metric_name)),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
        data = data_treatment,
        method = "ML",
        control = lmeControl(opt = "optim")
      )
    }, error = function(e) { message("      Error in treatment model: ", e$message); return(NULL) })

    message("    * Calculating control model")
    model_control <- tryCatch({
      lme(
        fixed = as.formula(sprintf("%s ~ time_saturated * (1 + base_slope + intervention_jump + intervention_slope)", metric_name)),
        random = ~ 1 + base_slope + intervention_slope + intervention_jump | id,
        data = data_control,
        method = "ML",
        control = lmeControl(opt = "optim")
      )
    }, error = function(e) { message("      Error in control model: ", e$message); return(NULL) })

    # skip if models did not converge
    if (is.null(model_treatment) || is.null(model_control)) {
      message("    * Skipped")
      next
    }

    df_treatment <- calculate_custom_effects(model_treatment, data_treatment)
    df_control   <- calculate_custom_effects(model_control, data_control)

    output_dir <- file.path("plots", "effects_distributions", metric_name)
    dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

    message("    * Creating plot")
    save_multipanel_png(
      expression = {
        par(oma = c(0, 0, 3, 0))
        plot_treated_vs_control_effects(df_control$intercept,                 df_treatment$intercept,                "Intercept")
        plot_treated_vs_control_effects(df_control$base_slope,                df_treatment$base_slope,               "Slope before Intervention")
        plot_treated_vs_control_effects(df_control$intervention_jump,         df_treatment$intervention_jump,        "Intervention Jump")
        plot_treated_vs_control_effects(df_control$slope_after_intervention,  df_treatment$slope_after_intervention, "Slope after Intervention")
        mtext(
          sprintf("Treatment and Control Effects Distributions - Project Age from %s to %s (Years)", min_project_age, max_project_age),
          outer = TRUE,
          cex = 1.5,
          font = 2
        )
      },
      file = file.path(output_dir, sprintf("%s.%s.%s.predicted.png", metric_name, min_project_age, max_project_age)),
    )


  }
}


# ------------------------------------------------------------------------------
# Testing
# ------------------------------------------------------------------------------




