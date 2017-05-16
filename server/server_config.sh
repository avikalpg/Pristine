# Code to install Python 3
# sudo apt-get install python3
#may be code to install pip

sudo pip install virtualenv
mkdir ENV_01
virtualenv ENV_01
cd ENV_01/
source bin/activate
pip install Django
pip install requests
pip install beautifulsoup4
python -m django --version
cd ..
django-admin startproject PristineServer

python manage.py startapp receiveCellInfo
python manage.py makemigrations receiveCellInfo

deactivate
