console.log("This is script file");

const toggleSiderbar = () => {


	if ($(".sidebar").is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};

const search = () => {
	console.log("searching...");

	let query = $("#search-input").val();


	if (query == "") {
		$(".search-result").hide();
	} else {

		console.log(query);
		//sending request to  server
		let url = `http://localhost:8282/search/${query}`;

		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			//data
			//console.log(data);

			let text = `<div class='list-group'>`;

			data.forEach((contact) => {
				text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;

			});

			text += `</div>`;

			$(".search-result").html(text);
			$(".search-result").show();
		});

		//search

		$(".search-result").show();

	}
};

//first request to  create order

const paymentStart = () => {
	console.log("Payment Started...");

	var amount = $("#payment_field").val();

	console.log(amount);

	if (amount == "" || amount == null) {
		// alert("Amount is required !!");
		swal("Failed !!","Amount is required !!","error");
		return;
	}

	// Use AJAX to send request to server to create order
	$.ajax({
		url: '/user/create_order',
		method: 'POST',
		contentType: 'application/json',
		data: JSON.stringify({ amount: amount, info: 'order_request' }),
		dataType: 'json',
		success: function (response) {
			// Invoked on success
			console.log(response);

			if (response.status == "created") {
				// open payment form

				let options = {

					key: 'rzp_test_2s4oCvzXkujAeg',
					amount: response.amount,
					currency: 'INR',
					name: 'Smart Contact Manager',
					description: 'Donation',
					image: 'https://iqraschool.edu.in/wp-content/uploads/2022/04/iqra-logo.png',
					order_id: response.id,
					handler: function (response) {
						console.log(response.razorpay_payment_id)
						console.log(response.razorpay_order_id)
						console.log(response.razorpay_signature)
						console.log('payment successful !!')
						// alert("congrates !! Payment successful")
						updatePaymentOnServer(
							response.razorpay_payment_id,
							response.razorpay_order_id,
							"paid"
						)
						
					},

					prefill: {
						name: "",
						email: "",
						contact: ""
					},

					notes: {
						address: "Learn With Majid"
					},

					theme: {
						color: "#3399cc",
					},


				};

				let rzp = new Razorpay(options);

				rzp.on("payment.failed",function(response){
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					// alert("Oops payment failed !!");
					swal("Failed !!","Oops payment failed !!","error");
				});

				rzp.open();

			}

		},
		error: function (error) {
			// Invoked on error
			console.log(error);
			alert("Something went wrong !!");
		}
	});


};


//update paymenton server
function updatePaymentOnServer(payment_id,order_id,status)
{

	$.ajax({
		url: '/user/update_order',
		method: 'POST',
		contentType: 'application/json',
		data: JSON.stringify({ payment_id: payment_id, order_id: order_id, status:status }),
		dataType: 'json',
		payment_id:payment_id,
		status:status,

		success: function(response) {
            swal("Payment success!!", "Congratulations! Payment successful", "success");
        },
        error: function(error) {
            swal("Failed !!", "Your payment is successful, but we did not capture it", "error");
        }
	});
	
	
}
