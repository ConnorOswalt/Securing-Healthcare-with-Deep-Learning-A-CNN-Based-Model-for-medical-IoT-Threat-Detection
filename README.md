# Securing Healthcare with Deep Learning: Enhanced CNN Model for CICIoMT2024 Threat Detection

<div align="center">

[![IEEE Conference](https://img.shields.io/badge/IEEE-ICIS%202024-blue.svg)](https://doi.org/10.1109/ICIS64839.2024.10887510)
[![arXiv](https://img.shields.io/badge/arXiv-2410.23306-b31b1b.svg)](https://arxiv.org/abs/2410.23306)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Python 3.12+](https://img.shields.io/badge/python-3.12+-blue.svg)](https://www.python.org/downloads/)
[![GPU Optimized](https://img.shields.io/badge/GPU-RTX%205070%20Ti-success.svg)](https://www.nvidia.com/en-us/geforce/graphics-cards/50-series/)

**Fork & Improvement Project** — Building on the original work for higher accuracy and expanded capabilities

</div>

---

## Overview

This repository is my personal fork and active improvement project based on the original implementation by Alireza Mohamadiam et al. (presented at ICIS 2024 and available at [original repo](https://github.com/alirezamohamadiam/Securing-Healthcare-with-Deep-Learning-A-CNN-Based-Model-for-medical-IoT-Threat-Detection)).

**My Goals & Changes:**
- Achieve even higher per-class accuracy, especially on minority attack types (Recon variants, certain MQTT sub-classes, Spoofing) where the original model shows lower F1 scores.
- Address class imbalance more effectively (class weights, SMOTE experimentation, focal loss exploration).
- Expand the model architecture (adding LSTM/GRU, attention mechanisms, hybrid ensembles).
- Optimize for modern hardware (RTX 5070 Ti Blackwell GPU in WSL2/Docker) with larger batch sizes (512–2048+), mixed precision, and tf.data pipelines for higher GPU utilization.
- Improve reproducibility: better model saving with unique filenames (including --class_config), checkpoints, and logging.
- Explore transfer learning to other IoT/medical datasets (e.g., TON_IoT, Edge-IIoTset, PhysioNet ECG anomalies).

The model continues to use the **CICIoMT2024** dataset (multi-protocol IoMT benchmark with Wi-Fi, MQTT, Bluetooth attacks on 40 devices) and supports 2-class (binary), 6-class, and 19-class classification.

**Current Best Results (my runs on RTX 5070 Ti):**
- Weighted F1 ~0.986–0.99
- Macro F1 improvements targeted on minorities
- Near 90%+ GPU utilization with tuned batch sizes

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