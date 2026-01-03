library(nlme)
library(performance)
library(tidyverse)
library(ggplot2)
library(effects)
library(tidyverse)
library(cowplot)
library(lme4)
library(sjPlot)
library(sjmisc)
library(effects)
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

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/shared.R")
source("R/saver.R")

fileNames <- list.files("data", pattern="*.csv", full.names=TRUE, recursive = TRUE)
metrics <- list(
  "findingsDensity",
  "longFilesDensity",
  "longMethodDensity",
  "deepNestingDensity",
  "cloneCoverage",
  "averageFileLoc"
  )

for (file_name in fileNames) {
  print(file_name)
  # Load Data
  data <- get_data_from_file(file_name)

  for (metric in metrics) {
    print(paste(" -",metric))

    # Create folder
    cleanedFileName <- gsub("data.", "", file_name)
    cleanedFileName <- gsub(".csv", "", cleanedFileName)
    folder_name <- paste("plots", "interrupted_time_series",cleanedFileName, metric, sep = "/")

    # Create model
    print(paste("   * Creating model"))
    model <- lme(
      fixed = as.formula(paste0(metric ," ~ 1 + t_pre + t_post + intervention")),
      random = ~ 1 + t_pre + t_post + intervention| id,
      # correlation = corAR1(form = ~ t | id),
      data = data,
      method="ML",
      control = lmeControl(opt = "optim")
    )


    save_everything(model, data, folder_name)

  }
}

# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------

source("R/plotting/plotting_project.R")
source("R/plotting/plotting_fixed_effects.R")
source("R/plotting/plotting_random_effects.R")
source("R/plotting/plotting_residuals.R")
source("R/plotting/plotting_general.R")
source("R/shared.R")
source("R/saver.R")


file_name <-"data/bucket_one/ALL.csv"
data <- get_data_from_file(file_name)

metric <- "findingsDensity"


no_quartiles <- 4  # You can change this to any number

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

# Calculate grid dimensions for plotting
plot_rows <- ceiling(sqrt(no_quartiles))
plot_cols <- ceiling(no_quartiles / plot_rows)


png(file.path("deleteme.png"), width = 20, height = 20, units = "cm", res = 300, pointsize = 8)
par(mfrow = c(plot_rows, plot_cols))

i <- 1
for (data_subset in splitted_data) {
  model <- lme(
    fixed = as.formula(paste0(metric, " ~ t_pre + t_post + intervention")),
    random = ~ 1 + t_pre + t_post + intervention | id,
    data = data_subset,
    method = "ML",
    control = lmeControl(opt = "optim")
  )

  save_everything(model, data_subset, paste0("delete_me/", i))
  i <- i+1

}



# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------


file_name <-"data/bucket_one/ALL.csv"
data <- get_data_from_file(file_name)
metric <- "findingsDensity"

model <- lme(
  fixed = as.formula(paste0(metric, " ~ t_pre + t_post + intervention")),
  random = ~ 1 + t_pre + t_post + intervention | id,
  data = data,
  method = "ML",
  control = lmeControl(opt = "optim")
)

source("R/plotting/plotting_fixed_effects.R")
plot_predicted_1(data_subset, model)

# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------

