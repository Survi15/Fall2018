from flask import Flask
from sqlalchemy import create_engine

app = Flask(__name__)
mysql = create_engine('mysql+pymysql://survi15:survi123@mysqldb.cwrcdvaet30p.us-west-2.rds.amazonaws.com:3306/User',
                      pool_size=312, pool_timeout=10000, connect_args={'connect_timeout': 10000})


@app.route('/<user_id>/<day>/<time_interval>/<step_count>', methods=['POST'])
def add_point(user_id, day, time_interval, step_count):
    conn = mysql.connect()
    trans = conn.begin()
    conn.execute("REPLACE INTO User.StepCount Values(%s, %s, %s, %s)" % (user_id, day, time_interval, step_count))
    trans.commit()
    trans.close()
    conn.close()
    return "S"


@app.route('/current/<user_id>', methods=['GET'])
def get_current_steps(user_id):
    conn = mysql.connect()
    result = conn.execute(
        "SELECT SUM(StepCount) from User.StepCount WHERE userID=%s "
        "GROUP BY day ORDER BY day DESC LIMIT 1" % user_id)
    result_sum = -1
    for row in result:
        result_sum = int(row[0]) if row[0] else -1
    result.close()
    conn.close()
    return str(result_sum)


@app.route('/single/<user_id>/<day>', methods=['GET'])
def get_single_steps(user_id, day):
    conn = mysql.connect()
    result = conn.execute("SELECT SUM(StepCount) from User.StepCount "
                          "WHERE userID=%s AND day=%s" % (user_id, day))
    result_sum = -1
    for row in result:
        result_sum = int(row[0]) if row[0] else -1
    result.close()
    conn.close()
    return str(result_sum)


@app.route('/range/<user_id>/<start_day>/<num_days>', methods=['GET'])
def get_range(user_id, start_day, num_days):
    end_day = int(start_day) + int(num_days) - 1
    result_list = []
    conn = mysql.connect()
    result = conn.execute(
        "SELECT SUM(StepCount) from User.StepCount WHERE userID=%s AND day>=%s and day <=%s "
        "GROUP BY day ORDER BY day" % (
            user_id, start_day, end_day))
    for row in result:
        result_list.append(int(row[0]) if row[0] else -1)
    result.close()
    conn.close()
    return str(result_list)


@app.route('/truncate',methods=['POST'])
def truncate():
    conn = mysql.connect()
    trans = conn.begin()
    conn.execute("TRUNCATE TABLE User.StepCount")
    trans.commit()
    trans.close()
    conn.close()
    return "S"


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=80, threaded=True)
