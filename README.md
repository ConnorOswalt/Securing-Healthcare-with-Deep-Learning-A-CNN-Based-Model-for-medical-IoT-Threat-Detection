# Securing Healthcare with Deep Learning: Enhanced CNN Model for CICIoMT2024 Threat Detection

<div align="center">

[![IEEE Conference](https://img.shields.io/badge/IEEE-ICIS%202024-blue.svg)](https://doi.org/10.1109/ICIS64839.2024.10887510)
[![arXiv](https://img.shields.io/badge/arXiv-2410.23306-b31b1b.svg)](https://arxiv.org/abs/2410.23306)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Python 3.7 - 3.12](https://img.shields.io/badge/python-3.7_to_3.12-blue.svg)](https://www.python.org/downloads/)
[![GPU Optimized](https://img.shields.io/badge/GPU-RTX%205070%20Ti-success.svg)](https://www.nvidia.com/en-us/geforce/graphics-cards/50-series/)

**Fork & Improvement Project** — Testing the robustness of the original model.

</div>

---

## Overview

This repository is my personal fork and active improvement project based on the original implementation by Alireza Mohamadiam et al. (presented at ICIS 2024 and available at [original repo](https://github.com/alirezamohamadiam/Securing-Healthcare-with-Deep-Learning-A-CNN-Based-Model-for-medical-IoT-Threat-Detection)).

## Archaeology

(Past Research) Paper referenced by our selected paper:

Liu, Y., & Latih, R. (2024). A Comprehensive Review of Machine
Learning Approaches for Detecting Malicious Software. International
Journal on Advanced Science, Engineering and Information
Technology.
https://ijaseit.insightsociety.org/index.php/ijaseit/article/view/19993


(Further Research) Paper that references our selected paper:

P. Choubey, Q. Shi, Z. Yang, B. Terry, S. Shao and T. Lei, "Deep Isolation Forest-based Anomaly Behavior Analysis for Internet of Medical Things," 2025 Cyber Awareness and Research Symposium (CARS), Grand Forks, ND, USA, 2025, pp. 1-7, doi: 10.1109/CARS67163.2025.11337811.
https://ieeexplore.ieee.org/abstract/document/11337811?casa_token=j1IvUF2fBAMAAAAA:DHyfA9JCi0G8Tt8OaqGYgFvOYA6nf7i6R_ULYhjnGeMA1gUDKq_g0FoOFS-RI_nC_ssJ2XS5VQ

## Run Instructions

required:
git
python 3.7 to 3.12 (not older or newer)
unix command line interface

Step 1. Clone this repository

git clone https://github.com/ConnorOswalt/Securing-Healthcare-with-Deep-Learning-A-CNN-Based-Model-for-medical-IoT-Threat-Detection.git

Step 2.

cd Securing-Healthcare-with-Deep-Learning-A-CNN-Based-Model-for-medical-IoT-Threat-Detection

Step 3.

replace /data with the stratified toy dataset (which is 20% of the original database, CiCIoMT2024):
https://www.dropbox.com/scl/fi/hq6lnx9wm6byh2nfjyk3k/CiCIoMT2024_Sample.zip?rlkey=6vpzh5nmamd5egf4tweut1chc&st=tyqhseaj&dl=0

Step 4. Install dependencies

pip install -r requirements.txt

Step 5.

cd src

Step 6. Run the program:

python main.py --class_config 19

Note: To change the level of noise inserted during the test phase, change NOISE_LEVEL in data_loader.py line 11 to a number between 0.0 and 1.0

---

## Original Work Citation

This project builds directly on the paper and code by Alireza Mohamadiam et al. Please cite their original work if you use or reference this repository:

```bibtex
@inproceedings{mohammadi2024securing,
  title={Securing Healthcare with Deep Learning: A CNN-Based Model for medical IoT Threat Detection},
  author={Mohammadi, Alireza and Ghahramani, Hosna and Asghari, Seyyed Amir and Aminian, Mehdi},
  booktitle={2024 19th Iranian Conference on Intelligent Systems (ICIS)},
  pages={168--173},
  year={2024},
  organization={IEEE}
}

IEEE Xplore: https://doi.org/10.1109/ICIS64839.2024.10887510
arXiv (open access): https://arxiv.org/abs/2410.23306
Original GitHub: https://github.com/alirezamohamadiam/Securing-Healthcare-with-Deep-Learning-A-CNN-Based-Model-for-medical-IoT-Threat-Detection


Performance Improvements (Ongoing)

  
  ![Current Results Summary](https://via.placeholder.com/800x400?text=My+Improved+Results+on+CICIoMT2024)
  *My latest weighted/macro F1 scores and per-class improvements*


Original paper reported ~0.99 weighted accuracy across tasks.
My focus: Raise macro F1 (especially low-recall classes like Recon-VulScan, MQTT-Publish_Flood) while maintaining high weighted performance.


Model Architecture (Enhanced)
Original: Simple Conv1D → MaxPool → Dense CNN
My expansions (in progress):

Hybrid CNN-LSTM/BiLSTM for temporal flow patterns
Multi-Head Attention for key feature focus
Mixed precision training (FP16) for faster RTX 50-series runs
Optional ensemble with RandomForest/XGBoost

See src/model.py for current implementation and experiments.

🚀 Quick Start (Fork Version)
1. Clone This Fork
Bashgit clone https://github.com/YOUR-USERNAME/YOUR-FORK-NAME.git
cd YOUR-FORK-NAME
(Replace with your actual repo URL once you create the fork.)
2. Install Dependencies
Bashpip install -r requirements.txt
# Optional extras for improvements
pip install imbalanced-learn tensorflow-addons keras-tuner
3. Download CICIoMT2024 Dataset
From official source:
🔗 https://www.unb.ca/cic/datasets/iomt-dataset-2024.html
(Direct download: http://cicresearch.ca/IOTDataset/CICIoMT2024/)
Place extracted CSV files (train/test splits) in:
textdata/
├── train/
└── test/
4. Run Training (with your improvements)
Bashcd src
python main.py --class_config 2    # Binary (attack/benign)
# or
python main.py --class_config 6    # Categorical
# or
python main.py --class_config 19   # Full multiclass
Models auto-save with unique names including --class_config value (e.g., cnn_cic_iomt2024_class-config-2_epochs-10_batch-512_2026-02-24_10-35-22.keras).

📂 Project Structure
text├── data/                     # Put train/test CSVs here
├── src/
│   ├── data_loader.py
│   ├── model.py              # Enhanced CNN + experiments
│   └── main.py               # Training script with unique saving
├── saved_models/             # Auto-created for model checkpoints
├── requirements.txt
└── README.md                 # This file

Next Steps & Contributions Welcome

Improve minority class F1 (Recon, certain MQTT attacks)
Add attention/LSTM hybrids
Test on other datasets (TON_IoT, PhysioNet)
Explore real-time inference on edge devices

Feel free to open issues/PRs if you'd like to collaborate!
⭐ Star this repo if it helps your IoMT security work! ⭐
Made in Houston, TX — Optimized for RTX hardware
Connor (fork maintainer)
