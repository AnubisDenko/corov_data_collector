import psycopg2 as ps
import subprocess


def get_db_conn():
    config = read_config()
    db_url = config['DATABASE_URL']
    return ps.connect(db_url)


def read_config():
    cmd = ['heroku', 'config']
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    o, e = proc.communicate()
    heroku_config = o.decode('ascii').replace('=== corov-loader Config Vars', '').replace(' ', '')
    lines = list(filter(lambda x: x != '', heroku_config.split("\n")))
    return dict(map(lambda x: x.split(":", 1), lines))
