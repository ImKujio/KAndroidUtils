package me.kujio.android.kandroidutils

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.kujio.android.kandroidutils.databinding.ItemLoadMoreBinding

fun EditText.showKeyboard() {
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    if (context !is Activity) return
    val imm = context.getSystemService(InputMethodManager::class.java) as InputMethodManager
    imm.showSoftInput(this, 0)
}

@BindingAdapter("simpleAdapter")
fun setSimpleAdapter(
    recyclerView: RecyclerView, simpleAdapter: SimpleRecyclerAdapter?
) {
    if (simpleAdapter == null) return
    recyclerView.recycledViewPool.setMaxRecycledViews(0, 8)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

@BindingAdapter("simpleAdapter", "poolSize")
fun setSimpleAdapter(
    recyclerView: RecyclerView, simpleAdapter: SimpleRecyclerAdapter?, poolSize: Int = 8
) {
    if (simpleAdapter == null) return
    recyclerView.recycledViewPool.setMaxRecycledViews(0, poolSize)
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

@BindingAdapter("simpleAdapter", "types", "poolSize")
fun setSimpleAdapter(
    recyclerView: RecyclerView, simpleAdapter: SimpleRecyclerAdapter?, types: Int = 1, poolSize: Int = 10
) {
    if (simpleAdapter == null) return
    repeat(types) { i ->
        recyclerView.recycledViewPool.setMaxRecycledViews(i, poolSize)
    }
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = simpleAdapter
}

class SimpleRecyclerAdapter(
    private val resId: (pos: Int) -> Int,
    private val count: () -> Int,
    private val bindData: (adapter: SimpleRecyclerAdapter, binding: ViewDataBinding, pos: Int) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerAdapter.SimpleViewHolder>() {
    lateinit var ctx: Context
    private val resIds = ArrayList<Int>()

    class SimpleViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        ctx = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SimpleViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), resIds[viewType], parent, false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        val id = resId(position)
        if (resIds.indexOf(id) == -1) resIds.add(id)
        return resIds.indexOf(id)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        bindData(this, holder.binding, position)
    }

    override fun getItemCount(): Int {
        return count()
    }
}

@BindingAdapter("loadMoreAdapter")
fun setLoadMoreAdapter(
    recyclerView: RecyclerView, loadMoreAdapter: LoadMoreRecyclerAdapter?
) {
    if (loadMoreAdapter == null) return
    if (recyclerView.context !is AppCompatActivity) return
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    recyclerView.adapter = loadMoreAdapter
}

class LoadMoreRecyclerAdapter(
    private val resId: (pos: Int) -> Int,
    private val count: () -> Int,
    private val load: suspend () -> Unit,
    private val more: () -> Boolean,
    private val bindData: (adapter: LoadMoreRecyclerAdapter, binding: ViewDataBinding, pos: Int) -> Unit
) : RecyclerView.Adapter<LoadMoreRecyclerAdapter.LoadMoreViewHolder>() {
    lateinit var ctx: Context

    class LoadMoreViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var loadMoreViewHolder: LoadMoreViewHolder

    private var scope: CoroutineScope? = null

    private var isLoading = false

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.context.let {
            ctx = it
            if (it is LifecycleOwner) scope = it.lifecycleScope
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = recyclerView.layoutManager?.childCount ?: 0
                val totalItemCount = recyclerView.layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    loadMoreViewHolder.binding.let { binding ->
                        binding as ItemLoadMoreBinding
                        if (more()) loadMore()
                        else binding.state = 2
                    }
                }
            }
        })
    }

    private fun loadMore() {
        if (isLoading) return
        isLoading = true
        loadMoreViewHolder.binding.let { binding ->
            binding as ItemLoadMoreBinding
            binding.state = 1
            scope?.launch {
                try {
                    load()
                    isLoading = false
                } catch (_: Throwable) {
                    binding.state = 3
                }
            } ?: run {
                isLoading = false
                binding.state = 3
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadMoreViewHolder {
        return LoadMoreViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), viewType, parent, false
            )
        ).also {
            if (viewType == R.layout.item_load_more)
                loadMoreViewHolder = it
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            R.layout.item_load_more
        } else {
            resId(position)
        }
    }

    override fun onBindViewHolder(holder: LoadMoreViewHolder, position: Int) {
        if (holder.binding is ItemLoadMoreBinding) {
            if (more()) loadMore()
            else holder.binding.state = 2
            holder.binding.load = View.OnClickListener {
                loadMore()
            }
        } else {
            bindData(this, holder.binding, position)
        }
    }

    override fun getItemCount(): Int {
        return count() + 1
    }
}