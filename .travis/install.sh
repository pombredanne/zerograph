#!/usr/bin/env bash


function python_install {
    VENV=$1
    PYTHON=$2
    mkdir -p $VENV
    virtualenv --python=$PYTHON $VENV
    source $VENV/bin/activate
    pip install -r requirements.txt
    deactivate
}

cd src/client/python
python_install venv/py27 `which python2.7`
python_install venv/py33 `which python3.3`

