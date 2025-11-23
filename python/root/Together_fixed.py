import pandas
from linearmodels.panel.results import PanelEffectsResults
from linearmodels import PanelOLS
import os
import seaborn
from matplotlib import pyplot
from pandas import DataFrame
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor

import Util


def main():
    project_names: list[str] = []
    for filename in os.listdir("csv"):
        if filename.endswith(".data.csv"):
            project_names.append(filename[:-len(".data.csv")])

    # Collect data from csvs
    # index: linter, value: list of dataframes with introduction dates
    print("Step 1: read data")
    all_data: dict[str, list[tuple[float, DataFrame]]] = {"CHECKSTYLE": [], "PMD": [], "SPOTBUGS": []}
    for project_name in project_names:

        linter_frame: DataFrame = pandas.read_csv("csv/{}.linter.csv".format(project_name), sep=',')
        for linter in Util.get_linters(linter_frame):
            data_frame: DataFrame = pandas.read_csv("csv/{}.data.csv".format(project_name), sep=',')
            data_frame.drop(data_frame.tail(1).index, inplace=True)

            linter_introduction_time: float = linter[0]
            linter_name: str = linter[1]

            data_frame["time"] = (data_frame["time"] - linter_introduction_time) / (60 * 60 * 24)
            data_frame['intervention'] = (data_frame['time'] >= 0).astype(int)
            data_frame['t_pre'] = data_frame['time'] * (1 - data_frame['intervention'])
            data_frame['t_post'] = data_frame['time'] * data_frame['intervention']
            data_frame = data_frame[(data_frame["time"] >= -730) & (data_frame["time"] <= 730)]
            data_frame['id'] = project_name
            all_data[linter_name].append((linter_introduction_time, data_frame))

    # Concat data
    print("Step 2: concatenate data")
    data_frames_per_linter: dict[str, DataFrame] = {}
    for linter_name in all_data:
        print("   - {0} data points for {1}".format(len(all_data[linter_name]), linter_name))
        df = pandas.DataFrame()
        for introduction_time, data_frame in all_data[linter_name]:
            df = pandas.concat([df, data_frame], axis=0)

        data_frames_per_linter[linter_name] = df

    # Create models and plots
    print("Step 3: create models and plots")
    for linter in data_frames_per_linter:
        print("   - starting with {0} data".format(linter))
        columns = pandas.read_csv("csv/{}.data.csv".format(project_names[0]), sep=',', index_col=[0]).columns
        data_frame = data_frames_per_linter[linter].sort_values(by='time')

        metrics_and_models: dict[str, tuple[DataFrame, PanelEffectsResults]] = {}
        print("      * creating models")
        for index, metric in enumerate(columns):
            data = data_frame[["id", "time", metric, "intervention", "t_pre", "t_post"]].dropna()

            # remove outliers
            Q1 = data[metric].quantile(0.25)
            Q3 = data[metric].quantile(0.75)
            IQR = Q3 - Q1

            lower_bound = Q1 - 1.5 * IQR
            upper_bound = Q3 + 1.5 * IQR

            data = data[(data[metric] >= lower_bound) & (data[metric] <= upper_bound)]

            data = data.set_index(['id', 'time'])

            # create Model
            formula = metric + ' ~ 1 + t_pre + t_post + intervention + C(id)'
            model: PanelEffectsResults = (PanelOLS.from_formula(formula, data=data, )
                                          .fit(cov_type='clustered', cluster_entity=True))

            metrics_and_models[metric] = (data, model)

        # plot
        print("      * creating plots")
        for metric in metrics_and_models:
            print(f"         * creating plots for {metric}")
            data: DataFrame = metrics_and_models[metric][0]
            model: PanelEffectsResults = metrics_and_models[metric][1]
            save_individual_plots(data, linter, metric, model)

    print("Finished")


def save_individual_plots(data: DataFrame, linter: str, metric: str, model: PanelEffectsResults):
    # 1. Calculate predictions
    data['predicted'] = model.predict()
    data_plot = data.reset_index()

    # Create the output directory
    output_dir = f"plots.fixed/{linter}/{metric}"
    os.makedirs(output_dir, exist_ok=True)

    # 2. Save the Model Summary to a separate file
    model_text = model.summary.as_text()
    summary_filepath = os.path.join(output_dir, f"_summary.txt")
    with open(summary_filepath, 'w') as f:
        f.write(model_text)

    # 3. Iterate and save individual plots
    unique_ids = data_plot['id'].unique()
    # parallel_plotting(unique_ids, data_plot, metric, output_dir)


def parallel_plotting(unique_ids, data_plot, metric, output_dir):
    with ProcessPoolExecutor(max_workers=16) as executor:
        futures = [executor.submit(create_and_save_plot, data_plot, metric, output_dir, proj_id)
                   for proj_id in unique_ids]
        for future in futures:
            future.result()


def create_and_save_plot(data_plot: DataFrame, metric: str, output_dir: str, proj_id):
    single_id_data = data_plot[data_plot['id'] == proj_id]

    # Plot
    fig, ax = pyplot.subplots(figsize=(8, 5))
    ax.scatter(single_id_data['time'], single_id_data[metric], alpha=0.6, label='Actual Data')
    ax.plot(single_id_data['time'], single_id_data['predicted'], color='red', label='Fitted Curve')
    ax.axvline(x=0, color='green', linestyle='--', linewidth=1, label='Intervention')

    ax.set_title(f'ITS Analysis for: {proj_id}')
    ax.set_xlabel('Time')
    ax.set_ylabel(metric)
    ax.legend()

    # Save
    plot_filepath = os.path.join(output_dir, f"{proj_id}.png")
    pyplot.tight_layout()
    pyplot.savefig(plot_filepath, dpi=300)
    pyplot.close(fig)


if __name__ == "__main__":
    main()
