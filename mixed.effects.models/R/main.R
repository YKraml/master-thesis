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
library(data.table)
library(clubSandwich)
library(latex2exp)

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

source("R/shared.R")
source("R/lagged_transition_time.R")
source("R/lagged_day_zero.R")
source("R/model_creator.R")

# ------------------------------------------------------------------------------
# Shared
# ------------------------------------------------------------------------------

languages <- list(
  java   = list(name = "java",   display_name ="Java",   color = "#7B68EE", color_secondary = "#D3CCFF"),
  python = list(name = "python", display_name ="Python", color = "#F8766D", color_secondary = "#FFD5D1"),
  cpp    = list(name = "cpp",    display_name ="C++",    color = "#00BFC4", color_secondary = "#CCF2F3")
)

metrics <- list(
  # All findings
  findingsDensity          = list(name = "findingsDensity",          display_name = "Findings Density",          unit = "Findings per 1 kLOC", interaction = "time_saturated"),

  # Findings groups
  comprehensibilityDensity = list(name = "comprehensibilityDensity", display_name = "Comprehensibility Density", unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  correctnessDensity       = list(name = "correctnessDensity",       display_name = "Correctness Density",       unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  documentationDensity     = list(name = "documentationDensity",     display_name = "Documentation Density",     unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  efficiencyDensity        = list(name = "efficiencyDensity",        display_name = "Efficiency Density",        unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  errorHandlingDensity     = list(name = "errorHandlingDensity",     display_name = "Error Handling Density",    unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  redundancyDensity        = list(name = "redundancyDensity",        display_name = "Redundancy Density",        unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  structureDensity         = list(name = "structureDensity",         display_name = "Structure Density",         unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  securityDensity          = list(name = "securityDensity",          display_name = "Security Density",          unit = "Findings per 1 kLOC", interaction = "time_saturated"),

  # Structure metrics
  longFiles                = list(name = "longFiles",                display_name = "Long Files",                unit = "Findings",            interaction = "relative_introduction_time"),
  longMethods              = list(name = "longMethods",              display_name = "Long Method",               unit = "Findings",            interaction = "relative_introduction_time"),
  deepNesting              = list(name = "deepNesting",              display_name = "Deep Nesting",              unit = "Findings",            interaction = "relative_introduction_time"),
  longFilesDensity         = list(name = "longFilesDensity",         display_name = "Long Files Density",        unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  longMethodDensity        = list(name = "longMethodDensity",        display_name = "Long Method Density",       unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  deepNestingDensity       = list(name = "deepNestingDensity",       display_name = "Deep Nesting Density",      unit = "Findings per 1 kLOC", interaction = "time_saturated"),
  cloneCoverage            = list(name = "cloneCoverage",            display_name = "Clone Coverage",            unit = "%",                   interaction = "time_saturated"),
  averageFileLoc           = list(name = "averageFileLoc",           display_name = "Average File Size",         unit = "LOC per File",        interaction = "time_saturated"),

  # Git metrics
  numberOfForks            = list(name = "numberOfForks",            display_name = "Number of Forks",            unit = "Forks",              interaction = "relative_introduction_time"),
  numberOfIssues           = list(name = "numberOfIssues",           display_name = "Number of Issues",           unit = "Issues",             interaction = "relative_introduction_time"),
  numberOfContributors     = list(name = "numberOfContributors",     display_name = "Number of Contributors",     unit = "Contributors",       interaction = "relative_introduction_time")
)








# ------------------------------------------------------------------------------
# Population Selection
# ------------------------------------------------------------------------------

java_data   <- read.csv("data/java/database_population/data.csv")
python_data <- read.csv("data/python/database_population/data.csv")
cpp_data    <- read.csv("data/cpp/database_population/data.csv")

out_dir <- file.path("plots", "database_population")
dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

save_png(
  expression = plot_merged_star_polygons(java_data, python_data, cpp_data),
  path = file.path(out_dir, "merged_stars.png"),
  width = 10,
  height = 8
)

save_png(
  expression = plot_star_hist(java_data, "Java", languages$java$color),
  path = file.path(out_dir, "java_stars.png"),
  width = 10,
  height = 8
)

save_png(
  expression = plot_star_hist(python_data, "Python", languages$python$color),
  path = file.path(out_dir, "python_stars.png"),
  width = 10,
  height = 8
)

save_png(
  expression = plot_star_hist(cpp_data, "C++", languages$cpp$color),
  path = file.path(out_dir, "cpp_stars.png"),
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
    path = file.path(out_dir, paste0(language_name, "_all_merged_non_balanced.png")),
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
    path = file.path(out_dir, paste0(language_name, "_all_merged.png")),
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
    path = file.path(overview_dir, paste0(language_name, ".all_metrics_boxplot.png")),
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
    path = file.path(overview_dir, paste0(language_name, ".all_metrics_boxplot_with_outliers.png")),
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

        filtered_data <- current_data[current_data <= upper_limit]

        h <- hist(filtered_data, breaks = 30, plot = FALSE)
        max_count <- max(h$counts)

        hist(
          filtered_data,
          breaks = 30,
          freq = TRUE,
          xlab = "",
          ylab = "",
          col = language_color,
          border = "black",
          main = metric_display_name,
          xlim = c(0, upper_limit * 1.1),
          ylim = c(0, max_count * 1.1)
        )
      }
    },
    path = file.path(overview_dir, paste0(language_name, ".all_metrics_histogram.png")),
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
        path = file.path(out_dir, paste0(prefix, "individuals.png")),
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
        path = file.path(out_dir, paste0(prefix, "scatter_hex.png")),
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
    tryCatch(
      {
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
      },
      error = function(e) {
        message("Error while comparing.", conditionMessage(e))
        return(NULL)
      })
    }
}



 # ------------------------------------------------------------------------------
# Lagged - Transition Time
# ------------------------------------------------------------------------------

for(language in languages) {
  language_name <- language$name
  language_color <- language$color
  language_color_secondary <- language$color_secondary
  message(language_name)

  out_dir <- file.path("plots", language_name, "lagged_transition_time")
  dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)

  for (metric in metrics) {
    metric_name <- metric$name
    message(metric_name)

    # data loading and caching
    csv_cache_path <- file.path(out_dir, sprintf("%s.results.csv", metric_name))
    results_matrix <- NULL

    if (file.exists(csv_cache_path)) {
      message(" - Loading from cache: ", csv_cache_path)
      # Use tryCatch in case the CSV exists but is empty/corrupt
      results_matrix <- tryCatch({
        data <- read.csv(csv_cache_path, check.names = FALSE)
        if(nrow(data) == 0) stop("Empty CSV")
        as.matrix(data)
      }, error = function(e) {
        message("   ! Cache corrupt or empty, recomputing...")
        return(NULL)
      })
    }

    # If cache didn't exist or was invalid, compute
    if (is.null(results_matrix)) {
      message(" - Computing results")
      results_list <- get_lagged_results(language_name, metric_name, 365)

      # Check if get_lagged_results returned an empty list or NULL
      if (is.null(results_list) || length(results_list) == 0) {
        warning(sprintf("   ! No data returned for %s in %s. Skipping.", metric_name, language_name))
        next # Skip to the next metric
      }

      results_matrix <- do.call(rbind, results_list)
      write.csv(results_matrix, csv_cache_path, row.names = FALSE)
    }

    # Double check matrix dimensions before plotting
    if (ncol(results_matrix) < 5) {
      warning(sprintf("   ! Results matrix for %s has insufficient columns. Skipping.", metric_name))
      next
    }



    x_values <- results_matrix[, 1]

    # Plot 1
    save_png(
      expression = {
        par(mfrow = c(2, 2), oma = c(0, 0, 0, 0), mar = c(5, 5, 2, 1))
        plot_lagged("Intercept", x_values, results_matrix[, 2], language_color)
        plot_lagged("Base Slope", x_values, results_matrix[, 3], language_color)
        plot_lagged("Level Change", x_values, results_matrix[, 4], language_color)
        plot_lagged("Slope Change", x_values, results_matrix[, 5], language_color)
      },
      path = file.path(out_dir, sprintf("%s.lagged.png", metric_name)),
      height = 10,
      width = 15
    )

    # Plot 2
    save_png(
      expression = {
        par(mfrow = c(2, 2), oma = c(0, 0, 0, 0), mar = c(5, 5, 2, 1))
        plot_max_abs_normalized_change("Intercept", x_values, results_matrix[, 2], language_color)
        plot_max_abs_normalized_change("Base Slope", x_values, results_matrix[, 3], language_color)
        plot_max_abs_normalized_change("Level Change", x_values, results_matrix[, 4], language_color)
        plot_max_abs_normalized_change("Slope Change", x_values, results_matrix[, 5], language_color)
      },
      path = file.path(out_dir, sprintf("%s.lagged_relative.png", metric_name)),
      height = 10,
      width = 15
    )
  }
}










# ------------------------------------------------------------------------------
# Model comparison - Interaction Term
# ------------------------------------------------------------------------------

for(language in languages){
  language_name <- language$name
  language_color <- language$color
  language_color_secondary <- language$color_secondary

  message(paste("---", language_name, "---"))

  output_dir <- file.path("plots", language_name, "time_interaction_comparison")
  dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)

  for (metric in metrics) {
    metric_name <- metric$name
    message(metric_name)

    file <- file.path(output_dir, sprintf("%s.%s.time_interaction_comparison.txt", language_name, metric_name))
    if (file.exists(file)) {
      next
    }

    file.exists(file)

    data <- get_data(language, "treatment", get_transition_time_in_days(language_name, metric_name))

    tryCatch({

      # Shared parameters
      effects <- "(base_slope + level_change + slope_change)"
      random_effects <- ~ 1 + base_slope + level_change + slope_change | id
      ctrl <- lmeControl(opt = "optim")

      message("1. Linear Model")
      f_lin <- as.formula(sprintf("%s ~ relative_introduction_time * %s", metric_name, effects))
      m_lin <- lme(fixed = f_lin, random = random_effects, data = data, method = "ML", control = ctrl)

      message("2. Saturation Model")
      f_sat <- as.formula(sprintf("%s ~ time_saturated * %s", metric_name, effects))
      m_sat <- lme(fixed = f_sat, random = random_effects, data = data, method = "ML", control = ctrl)

      message("3. Logarithmic Model")
      f_log <- as.formula(sprintf("%s ~ time_log * %s", metric_name, effects))
      m_log <- lme(fixed = f_log, random = random_effects, data = data, method = "ML", control = ctrl)

      message("4. Exponential Model")
      f_exp <- as.formula(sprintf("%s ~ time_e * %s", metric_name, effects))
      m_exp <- lme(fixed = f_log, random = random_effects, data = data, method = "ML", control = ctrl)

      message("5. Polynomial Models (Degrees 1 to 3)")
      f_poly1 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 1) * %s", metric_name, effects))
      m_poly1 <- lme(fixed = f_poly1, random = random_effects, data = data, method = "ML", control = ctrl)

      f_poly2 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 2) * %s", metric_name, effects))
      m_poly2 <- lme(fixed = f_poly2, random = random_effects, data = data, method = "ML", control = ctrl)

      f_poly3 <- as.formula(sprintf("%s ~ poly(relative_introduction_time, 3) * %s", metric_name, effects))
      m_poly3 <- lme(fixed = f_poly3, random = random_effects, data = data, method = "ML", control = ctrl)


      message("6. Natural Spline Models (Degrees of Freedom 1 to 3)")
      f_spline1 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 1) * %s", metric_name, effects))
      m_spline1 <- lme(fixed = f_spline1, random = random_effects, data = data, method = "ML", control = ctrl)

      f_spline2 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 2) * %s", metric_name, effects))
      m_spline2 <- lme(fixed = f_spline2, random = random_effects, data = data, method = "ML", control = ctrl)

      f_spline3 <- as.formula(sprintf("%s ~ ns(relative_introduction_time, df = 3) * %s", metric_name, effects))
      m_spline3 <- lme(fixed = f_spline3, random = random_effects, data = data, method = "ML", control = ctrl)


      message("6. Compare models")
      model_comparison <- anova(m_lin, m_sat, m_log, m_exp, m_poly1, m_poly2, m_poly3, m_spline1, m_spline2, m_spline3)

      # Save results sorted by AIC
      capture.output(
        model_comparison[order(model_comparison$AIC), ],
        file = file
      )


    }, error = function(e) { message("Error: ", e$message); return(NULL) })


  }

}


# ------------------------------------------------------------------------------
# Interrupted Time Series Loop
# ------------------------------------------------------------------------------


for(language in languages) {
  language_name <- language$name
  language_color <- language$color
  language_color_secondary <- language$color_secondary
  message(paste("---", language_name, "---"))


  for (metric_data in metrics) {
    metric_name         <- metric_data$name
    metric_display_name <- metric_data$display_name
    metric_unit         <- metric_data$unit
    interaction         <- metric_data$interaction

    message(metric_name)

    for(group in c("treatment", "control")){
      message(sprintf(" - %s ", group))

      # tryCatch({

        message("   * Loading Data")
        transition_n_days <- get_transition_time_in_days(language_name, metric_name)
        data <- get_data(language, group, transition_n_days)
        data <- data[complete.cases(data), ]


        message("   * Creating Folder")
        out_dir <- file.path("plots", language_name, "interrupted_time_series", group, metric_name)
        dir.create(out_dir, showWarnings = FALSE, recursive = TRUE)
        prefix <- sprintf("%s.%s.%s.",language_name, group, metric_name)

        message("   * Getting Model")
        model <- get_model(data, language_name, group, metric_name, interaction)


        message("   * Extracting Effects")
        random_effects <- ranef(model)
        fixed_effects <- fixef(model)


        message("   * Saving Summary")
        capture.output(
          summary(model),
          file = file.path(out_dir, paste0(prefix, "model_summary.txt"))
        )


        # 1. print model sensitivity
        message("   * Start Plotting")
        save_png(
          expression = plot_model_sensitivity(model, interaction, language_color),
          path = file.path(out_dir, paste0(prefix, "sensitivity.png"))
        )

        # 2. print fixed effects
        save_multipanel_png(
          expression = {
            plot_predicted(data, model, 0, metric_display_name, metric_unit, transition_n_days, interaction)
            plot_predicted(data, model, 0.5, metric_display_name, metric_unit, transition_n_days, interaction)
            plot_predicted(data, model, 1  , metric_display_name, metric_unit, transition_n_days, interaction)
            plot_predicted(data, model, 2, metric_display_name, metric_unit, transition_n_days, interaction)
            plot_predicted(data, model, 3, metric_display_name, metric_unit, transition_n_days, interaction)
            plot_predicted(data, model, 4, metric_display_name, metric_unit, transition_n_days, interaction)
          },
          path = file.path(out_dir, paste0(prefix, "predicted.png")),
          mfrow = c(3, 2),
          width = 20,
          height = 25
        )




        # 3. print qq of residuals
        save_png(
          expression = plot_residuals_qq(data, model),
          path = file.path(out_dir, paste0(prefix, "residual_qq.png"))
        )

        # 4. print residuals vs fitted
        save_png(
          expression = plot_residuals_vs_fitted(data, model, language_color, metric_display_name, metric_unit),
          path = file.path(out_dir, paste0(prefix, "residual_vs_fitted.png")),
          width = 10,
          height = 8
        )

        # 5. print residuals vs time
        save_png(
          expression = plot_residuals_vs_time(data, model, language_color),
          path = file.path(out_dir, paste0(prefix, "residual_vs_time.png")),
          width = 10,
          height = 8
        )

        # 6. print residuals vs age
        save_png(
          expression = plot_residuals_vs_age(data, model, language_color),
          path = file.path(out_dir, paste0(prefix, "residual_vs_age.png")),
          width = 10,
          height = 8
        )

        # X. print residuals vs intervention
        save_png(
          expression = plot_residuals_vs_intervention(data, model, language_color),
          path = file.path(out_dir, paste0(prefix, "residual_vs_intervention.png")),
          width = 10,
          height = 8
        )

        save_multipanel_png(
          expression = {
            plot_residual_histogram(data, model, 0, language_color)
            plot_residual_histogram(data, model, 1, language_color)
          },
          path = file.path(out_dir, paste0(prefix, "residual_vs_intervention_hist.png")),
          mfrow = c(1, 2),
          width = 15,
          height = 8
        )




        # 7. print residuals histogram
        save_png(
          expression = plot_residuals_histogram(data, model, language_color),
          path = file.path(out_dir, paste0(prefix, "residual_hist.png")),
          width = 10,
          height = 8
        )

        # 8. print Auto- and Cross- Covariance and -Correlation Function Estimation
        save_png(
          expression = acf(resid(model, type = "normalized")),
          path = file.path(out_dir, paste0(prefix, "acf.png")),
          width = 10,
          height = 8
        )

        # 9. print random effects correlation
        save_png(
          expression = plot_random_effects_correlation(model, metric_display_name, language_color),
          path = file.path(out_dir, paste0(prefix, "random_effects_correlation.png"))
        )

        # 10. print random effects histograms
        save_multipanel_png(
          expression = {
            plot_random_effects_histogram(random_effects$`(Intercept)`, "lightblue", "Intercept", random_effects["(Intercept)"])
            plot_random_effects_histogram(random_effects$base_slope, "lightgreen", "Base Slope", random_effects["base_slope"])
            plot_random_effects_histogram(random_effects$slope_change, "darkseagreen1", "Slope Change", random_effects["slope_change"])
            plot_random_effects_histogram(random_effects$level_change, "paleturquoise1", "Level Change", random_effects["level_change"])
          },
          path = file.path(out_dir, paste0(prefix, "random_effect_hist.png")),
          width = 20,
          height = 14
        )

        # 11. print random effects qq-plot
        save_multipanel_png(
          expression = {
            qqnorm(random_effects[, "(Intercept)"], main = "Intercept"); qqline(random_effects[, "(Intercept)"])
            qqnorm(random_effects[, "base_slope"], main = "Base Slope"); qqline(random_effects[, "base_slope"])
            qqnorm(random_effects[, "slope_change"], main = "Slope Change"); qqline(random_effects[, "slope_change"])
            qqnorm(random_effects[, "level_change"], main = "Level Change"); qqline(random_effects[, "level_change"])
          },
          path = file.path(out_dir, paste0(prefix, "random_effects_qq.png")),
        )

        # 12. print random effects vs residuals
        effects <- c("(Intercept)", "base_slope", "slope_change", "level_change")
        plots <- lapply(effects, function(eff) plot_random_effects_vs_residuals(data, model, eff))
        save_png(
          expression = print((plots[[1]] | plots[[2]]) / (plots[[3]] | plots[[4]])),
          path = file.path(out_dir, paste0(prefix, "random_effects_residuals.png")),
          width = 30,
          height = 20
        )



        # 13. print prediction for project
        save_multipanel_png(
          expression = {

            i <- 0
            for (id in unique(data[["id"]])) {
              plot_project(model, id, data, metric_name, metric_unit, metric_display_name, transition_n_days, interaction, language_color)
              i <- i + 1
              if(i >= 8){
                break
              }
            }

          },
          path = file.path(out_dir, paste0(prefix, "projects.png")),
          mfrow = c(4, 2),
          width = 20,
          height = 28
        )



      # }, error = function(e) {
      #   warning_msg <- sprintf("FAILURE in Metric: '%s' | Group: '%s'", metric_name, group)
      #   message(paste0("\n ", warning_msg, "\n Error message: ", e$message, "\n"))
      # })
    }
  }
}



# ------------------------------------------------------------------------------
# Treated vs Control Effects Distributions
# ------------------------------------------------------------------------------

age_ranges <- list(
  c(0, 0.3),
  c(0.3, 1),
  c(1, 2),
  c(2, 20)
)

for(language in languages) {
  language_name <- language$name
  language_color <- language$color
  language_color_secondary <- language$color_secondary
  message(paste("---", language_name, "---"))

  for (metric_data in metrics) {

    tryCatch({

    metric_name         <- metric_data$name
    metric_display_name <- metric_data$display_name
    metric_unit         <- metric_data$unit
    interaction         <- metric_data$interaction

    message(metric_name)


    message("   * Creating folder")
    output_dir <- file.path("plots", language_name, "effects_distributions", metric_name)
    dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)


    message("   * Loading Data")
    transition_n_days <- get_transition_time_in_days(language_name, metric_name)
    data_treatment <- get_data(language, "treatment", transition_n_days)
    data_control   <- get_data(language, "control",   transition_n_days)

    message("   * Loading Models")
    model_treatment <- get_model(data_treatment, language_name, "treatment", metric_name, interaction)
    model_control   <- get_model(data_control,   language_name, "control",   metric_name, interaction)

    message("    * Creating plots")
    for (range in age_ranges) {
      min_project_age <- range[1]
      max_project_age <- range[2]

      df_treatment <- calculate_custom_effects(model_treatment, data_treatment, min_project_age, max_project_age, interaction)
      df_control   <- calculate_custom_effects(model_control,   data_control,   min_project_age, max_project_age, interaction)

      save_multipanel_png(
        expression = {
          plot_treated_vs_control_effects(df_control$intercept,                 df_treatment$intercept,                "Intercept")
          plot_treated_vs_control_effects(df_control$base_slope,                df_treatment$base_slope,               "Base Slope")
          plot_treated_vs_control_effects(df_control$level_change,              df_treatment$level_change,             "Level Change")
          plot_treated_vs_control_effects(df_control$slope_after_intervention,  df_treatment$slope_after_intervention, "Slope after Intervention")
        },
        path = file.path(output_dir, sprintf("%s.%s.%s.random_effects_comparison.png", metric_name, min_project_age, max_project_age)),
        mfrow = c(2, 2),
        width = 20,
        height = 13
      )
    }

  }, error = function(e) {
    warning_msg <- sprintf("FAILURE in Metric: '%s'", metric_name)
    message(paste0("\n ", warning_msg, "\n Error message: ", e$message, "\n"))
  })

  }
}


# ------------------------------------------------------------------------------
# Testing
# ------------------------------------------------------------------------------




