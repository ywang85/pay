<html>
<head>
    <meta charset="UTF-8">
    <title>"支付"</title>
</head>
<body>
<script src="https://cdn.bootcss.com/jquery/1.5.1/jquery.min.js"></script>
<script src="https://cdn.bootcss.com/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>
<div id="myQrcode"></div>
<div id="orderId" hidden>${orderId}</div>
<div id="returnUrl" hidden>${returnUrl}</div>
<script>
    jQuery('#myQrcode').qrcode({
        text:"${codeUrl}"
    });

    $(function () {
        // 定时器轮询
        setInterval(function() {
            console.log("开始查询支付状态")
            $.ajax({
                url: "queryByOrderId",
                data: {
                    "orderId": $("#orderId").text()
                },
                success: function (result) {
                    console.log(result)
                    if (result.platformStatus != null && result.platformStatus === "SUCCESS") {
                        location.href = $("#returnUrl").text()
                    }
                },
                error: function (result) {
                    alert(result)
                }
            })
        }, 2000)
    })
</script>
</body>
</html>
